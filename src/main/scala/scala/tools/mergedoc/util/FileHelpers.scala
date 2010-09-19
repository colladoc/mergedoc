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
package util

import java.io.{FileReader, BufferedReader, FileWriter, File}

/**
 * Provides utility functions for working with files..
 * @author Petr Hosek
 */
trait FileHelpers {

  /** Transforms a file to extended file providing fluent interface. */
  implicit def toFile(c: File) = new FileExtensions(c)

  /**
   * Extends File class to provide more fluent interface.
   */
  class FileExtensions(f: File) {
    /** Write string into a file. */
    def write(text: String): Unit = {
      val writer = new FileWriter(f)
      try {
        writer.write(text)
      } finally { writer.close }
    }

    /** Loop through each line in file. */
    def foreachLine(func: String => Unit): Unit = {
      val reader = new BufferedReader(new FileReader(f))
      try {
        while(reader.ready)
          func(reader.readLine)
      } finally { reader.close }
    }

    /** Delete directory recursively. */
    def deleteRec: Unit = {
      def deleteFile(file: File): Unit = {
        if (file.isDirectory){
          val subfiles = file.listFiles
          if(subfiles != null)
            subfiles.foreach { f => deleteFile(f) }
        }
        file.delete
      }
      deleteFile(f)
    }
  }

}