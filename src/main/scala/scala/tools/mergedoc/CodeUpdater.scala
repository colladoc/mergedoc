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

import scala.collection.mutable.Map
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.LinkedList

import org.apache.commons.lang.WordUtils

import java.io.File
import text.edits.{MultiTextEdit, TextEdit, ReplaceTextEdit}
import text.TextFile
import io.Source

/**
 * Source code updater.
 * @author Donna Malayeri, Petr Hosek
 */
class CodeUpdater(processor: CodeProcessor) {

  import processor._

  def update = for ((file, edit) <- process) {
    val text = new TextFile(file)
    Console.println("Processing " + file.getName)
    if (config.verbose) {
      Console.println(edit.toString)
    }
    text.perform(edit)
    text.save
  }

  protected def process = {
    val changes = Map.empty[File, MultiTextEdit]
    for (item <- items) item match {
      case NotFound(id, filename) => // error("Error finding " + id + " in file " + filename)
      case NoPosition(id) => // warning("Unable to determine position for " + id)
      case FoundSymbol(loc) =>
        val path = config.path + File.separator + loc.filename
        change(path, loc.offset, loc.length, loc.comment, loc.newLine) match {
          case Some((file, edit)) => changes.get(file) match {
            case Some(multi) =>
              multi.add(edit)
            case None =>
              val multi = new MultiTextEdit
              multi.add(edit)
              changes += file -> multi
          }
          case None => // warning("Could not open file " + path)
        }
    }
    changes
  }

  private def change(filename: String, pos: Int, len: Int, text: String, newline: Boolean) = {
    val file = new File(filename)
		if (file.exists()) {
      val ind = tabs(file, pos)
			val end = if (newline) "\n" + ind + " */\n" + ind
				         else "\n" + ind + " */"
      val cmt = WordUtils.wrap(text, config.lineWidth).split("\n")
      val repl = cmt.mkString("/** ", "\n" + ind + " *  ", end)

			Some((file, new ReplaceTextEdit(pos, len, repl)))
		}
		else
			None
	}

  private def tabs(file: File, pos: Int) = {
    val stream = Source.fromFile(file).toStream
    stream.take(pos).reverse.takeWhile(c => c == ' ' || c == '\t').mkString
  }

}