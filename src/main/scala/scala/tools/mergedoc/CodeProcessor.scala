/*
 * Copyright (c) 2010, Petr Hosek. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 *     the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COLLABORATIVE SCALADOC PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COLLABORATIVE SCALADOC
 * PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scala.tools.mergedoc

import source.ChangeSource
import source.EntityType

import scala.tools.nsc._
import scala.tools.nsc.interactive._
import scala.tools.nsc.symtab._
import scala.tools.nsc.reporters.{Reporter, ConsoleReporter}
import scala.tools.nsc.util.{FakePos, Position, NoPosition}
import scala.tools.nsc.io.AbstractFile

import java.io.File

/**
 * Source code processor.
 * @author Donna Malayeri, Petr Hosek
 */
class CodeProcessor(val config: Configuration, source: ChangeSource) {

  def error(msg: String) {
    reporter.error(FakePos("scalac"), msg + "\n  scalac -help  gives more information")
  }

  val docSettings = new Settings(error _)
  val reporter = new ConsoleReporter(docSettings)
  
	object compiler extends interactive.Global(docSettings, reporter) with interactive.RangePositions {
	  override protected def computeInternalPhases() {
	    phasesSet += syntaxAnalyzer
      phasesSet += analyzer.namerFactory
      phasesSet += analyzer.packageObjects
      phasesSet += analyzer.typerFactory
	  }
	  override def onlyPresentation = true
	}

  abstract class SymbolResult
  case class FoundSymbol(location: Location) extends SymbolResult
  case class NotFound(id: String, filename: String) extends SymbolResult
  case class NoPosition(id: String) extends SymbolResult

  case class Location(filename: String, id: String, offset: Int, length: Int, comment: String, newLine: Boolean)

	def items: Iterable[SymbolResult] = {
    if (config.verbose)
      println("Fetching changes from " + source)
		for (change <- source.changes) yield {
      val id = format(change.id, change.entity)
			val path = config.path + File.separator + change.filename

      val cmd = new CompilerCommand("-classpath" :: config.classpath :: path :: Nil, docSettings)
      new compiler.Run() compile cmd.files

			findSymbol(change.entity, change.filename, change.id) match {
        case Some(sym) =>
					getOffset(sym) match {
						case Some((off, len, existed)) =>
							FoundSymbol(new Location(change.filename, id, off, len, change.comment, !existed))
						case None =>
              NoPosition(id)
					}
        case None =>
          NotFound(id, path)
			}
		}
	}

  def findSymbol(entityType: EntityType.Type, path: String, id: String): Option[compiler.Symbol] = {
    def doFind(path: List[String], current: List[compiler.Symbol]): List[compiler.Symbol] = path match {
      case Nil => current
      case head :: tail =>
        current.flatMap(symbol => {
            val symbols = symbol.info.members filter { _.simpleName.toString == head }
            doFind(tail, symbols.toList)
        })
    }

		val openIdx = id.indexOf('(')
		var typeList: List[String] = Nil

		val identifier: String =
			if (openIdx >= 0) {
				val closeIdx = id.indexOf(')')
				if (closeIdx >= 0) {
					typeList = id.substring(openIdx + 1, closeIdx) match {
						case s if s.isEmpty => Nil
						case s => s.split(",").toList.map(_.trim)
					}
					id.substring(0, openIdx)
				} else {
					id // will fail later with the open paren in the name
				}
			}
			else
				id

    val roots = compiler.definitions.RootPackage :: compiler.definitions.EmptyPackage :: Nil
		val matchingSyms = doFind(identifier.split(Array('.', '#')).toList, roots).filter(sym => entityType match {
      case EntityType._package => sym.isPackageObject
      case EntityType._trait => sym.isTrait
      case EntityType._class => sym.isClass
      case EntityType._object => sym.isModule
      case EntityType.value => sym.isMethod || sym.isGetter
      case EntityType._type => sym.isAbstractType || sym.isAliasType
      case _ => false
    })

		matchingSyms match {
			case Nil => None
			case sym :: Nil => Some(sym)
			case _ => // list has at least 2 elements; overloads found, see if there's a parameter list
				if (openIdx >= 0) matchingSyms.find(symbol => {
          if (typeList.isEmpty && symbol.info.paramTypes.isEmpty)
            true
          else
            (typeList.length == symbol.info.paramTypes.length && (typeList, symbol.info.paramTypes).zipped.exists(_ == _.toString))
        }) else None
		}
	}

  /**
   * Returns the source offset, length, and whether or not a comment existed already.
	*/
	def getOffset(sym: compiler.Symbol): Option[(Int, Int, Boolean)] =
    compiler.docComments.get(sym) match {
      case Some(cmt) =>
        cmt.pos match {
          case p: Position if p eq NoPosition => None
          case p: Position => Some(p.startOrPoint, cmt.raw.length, true)
        }
      case None =>
        if (sym.pos.isRange)
          Some(sym.pos.start, 0, false)
        else
          None
    }

	def format(identifier: String, entityType: EntityType.Type): String = {
		val pos = identifier.lastIndexOf(".")
		entityType + " " + identifier.substring(pos + 1)
	}

	def done = { compiler.askShutdown }

}