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

import java.io.File
import io.Source
import source.{ChangeSource, FileChangeSource, UrlChangeSource}

object Mergedoc {

  var hasErrors = false

  def main(args: Array[String]): Unit = {
    process(args)
    exit(if (hasErrors) 1 else 0)
  }

  def process(args: Array[String]): Unit = {
    val parser = new ArgumentParser
    val arguments = args.toList map parser.getArgument
    var errors: List[String] = Nil

    if (arguments contains Help) {
      printUsage()
      return
    }

    for (BadOption(s) <- arguments)
      errors ::= "Unrecognised option: " + s

    def getFiles(): List[File] = {
      var files: List[File] = Nil
      def addFile(fileName: String) {
        val file = new File(fileName)
        if (!file.exists)
          errors ::= "No such file " + file
        if (file.isDirectory)
          errors ::= "Cannot format a directory (" + file + ")"
        files ::= file
      }
      for (FileList(listName) <- arguments) {
        val listFile = new File(listName)
        if (!listFile.exists)
          errors ::= "No such file: file list " + listFile
        else if (listFile.isDirectory)
          errors ::= "Path is a directory: file list " + listFile
        else
          Source.fromFile(listFile).getLines foreach addFile
      }
      for (FileName(fileName) <- arguments) addFile(fileName)
      files.reverse
    }

    val files = getFiles()

    val interactive = arguments contains Interactive
    val verbose = arguments contains Verbose

    val filename = arguments collect { case FileName(n) => n } headOption
    val url = arguments collect { case Url(u) => u } headOption

    val sources = List(
      filename match {
        case Some(f) => Some(new FileChangeSource(f))
        case None => None
      }, url match {
        case Some(u) => Some(new UrlChangeSource(u))
        case None => None
      }).flatten

    if (sources.isEmpty)
      errors ::= "File or url list is empty"

    var path: String = "" // TODO: simplify
    arguments collect { case Path(p) => p } headOption match {
      case Some(p) => path = p
      case None => errors ::= "Path is empty"
    }

    if (!errors.isEmpty) {
      errors.reverse foreach System.err.println
      hasErrors = true
      return
    }

    val classpath = "/home/hosekp/Development/collaborative-scaladoc/mergedoc/project/boot/scala-2.8.0/lib/scala-library.jar" // System.getProperties.getProperty("java.class.path")
    val settings = new Configuration(classpath, path, 80, interactive, verbose)
    val processor = new CodeProcessor(settings, sources.head)
		val updater = new CodeUpdater(processor)
    updater.update
    processor.done
  }

  private def printUsage() {
    println("Usage: mergedoc [options] [files...]")
    println()
    println("Options:")
    println("  --help, -h                      Show help.")
    println("  --interactive, -i               Ask for confirmation on each change.")
    println("  --file=<file>, -f=<file>        File source containing documentation changes.")
    println("  --path=<path>, -p=<path>        Path to sources to which changes should be applied.")
    println("  --url=<url>,   -u=<url>         Url containing documentation changes.")
    println("  --verbose -v                    Verbose output.")
    println()
  }

}
