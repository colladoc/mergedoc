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
package text
package edits

import tools.mergedoc.text.TextFileStore

abstract class TextEdit(val off: Int, val len: Int) {
  require(off >= 0)
  require(len >= 0)

  def apply(store: TextFileStore)

  override def toString =
    "{%s}[%s, %s]".format(getClass.getSimpleName, off, len)
}

class InsertTextEdit(off: Int, len: Int, text: String) extends TextEdit(off, len) {
  def apply(store: TextFileStore) =
    store.replace(off, 0, text)

  override def toString =
    super.toString + " << " + text
}

class DeleteTextEdit(off: Int, len: Int) extends TextEdit(off, len) {
  def apply(store: TextFileStore) =
    store.replace(off, len, "")
}

class ReplaceTextEdit(off: Int, len: Int, text: String) extends TextEdit(off, len) {
  def apply(store: TextFileStore) =
    store.replace(off, len, text)

  override def toString =
    super.toString + " << " + text
}

class MultiTextEdit extends TextEdit(0, 0) {

  var edits = List.empty[TextEdit]

  def apply(store: TextFileStore) =
    edits.foreach { _.apply(store) }

  def covers(edit: TextEdit) =
    edits.contains(edit)

  def add(edit: TextEdit) =
    edits ::= edit

  def remove(edit: TextEdit) =
    edits = edits filterNot (_ == edit)

  override def toString =
    edits.mkString("\n")

}