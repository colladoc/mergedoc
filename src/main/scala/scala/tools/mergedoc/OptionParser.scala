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

import scala.util.parsing.combinator.{RegexParsers, JavaTokenParsers}

class ArgumentParser extends JavaTokenParsers with RegexParsers {

  lazy val arguments: Parser[Argument] =
    phrase(help) | phrase(interactive) | phrase(verbose) | phrase(file) | phrase(path) | phrase(fileList) |
      phrase(toggle) | phrase(preferenceArgument) | phrase(badOption)

  lazy val interactive = ("--interactive" | "-i") ^^^ Interactive
  lazy val verbose = ("--verbose" | "-v") ^^^ Verbose
  lazy val help = ("--help" | "-help" | "-h") ^^^ Help
  lazy val file = ("--file=" | "-f=") ~ ".+".r ^^ { case (_ ~ path) => FileName(path) }
  lazy val path = ("--path=" | "-p=") ~ ".+".r ^^ { case (_ ~ path) => Path(path) }
  lazy val fileList = ("--fileList=" | "-l=") ~ ".+".r ^^ { case (_ ~ name) => FileList(name) }
  
  lazy val toggle = plusOrMinus ~ ident ^^ { case onOrOff ~ key => PreferenceArgument(key, onOrOff.toString) }
  lazy val plusOrMinus = "+" ^^^ true | "-" ^^^ false
  lazy val preferenceArgument = "-" ~ ident ~ "=" ~ "\\w+".r ^^ { case (_ ~ key ~ _ ~ value) => PreferenceArgument(key, value) }
  lazy val badOption = guard(plusOrMinus) ~> ".*".r ^^ { BadOption(_) }

  def getArgument(s: String) = parse(arguments, s) getOrElse FileName(s)
}

sealed trait Argument

case class PreferenceArgument(preferenceKey: String, value: String) extends Argument
case class FileName(name: String) extends Argument
case class FileList(name: String) extends Argument
case class Path(path: String) extends Argument

case object Interactive extends Argument
case object Verbose extends Argument
case object Help extends Argument

case class BadOption(name: String) extends Argument