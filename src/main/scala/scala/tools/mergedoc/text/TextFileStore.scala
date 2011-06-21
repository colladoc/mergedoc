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
package scala.tools.mergedoc.text

class TextFileStore(var text: String) {

  case class Edit(text: String, len: Int, var off: Int)

  var edits = List.empty[Edit]

  def replace(off: Int, len: Int, text: String) = {
    def doInsert(list: List[Edit], shift: Int): List[Edit] = list match {
      case head :: tail if head.off < off =>
        val delta = head.text.length - head.len
        head :: doInsert(tail, shift + delta)
      case head :: tail =>
        val delta = text.length - len
        tail.foreach(r => r.off += delta)
        Edit(text, len, off + shift) :: head :: tail
      case Nil =>
        Edit(text, len, off) :: Nil
    }
    edits = doInsert(edits, 0)
  }

  object processor {

    def run(text: String, edits: List[Edit]) = edits match {
      case Nil =>
        text
      case _ =>
        val builder = new StringBuilder
        var delta = 0
        for (edit <- edits) {
          val off = builder.length - delta
          builder.append(text.substring(off, edit.off))
          builder.append(edit.text)
          delta += edit.text.length - edit.len
        }
        val off = builder.length - delta
        builder.append(text.substring(off, text.length))
        builder.toString
    }

  }

  override def toString =
    processor.run(text, edits)
  
}