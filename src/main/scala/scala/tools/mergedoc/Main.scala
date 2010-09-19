package scala.tools.mergedoc

import java.io.File
import io.Source


//object Main {
//
//  def main(args: Array[String]) {
//    val parser = new ArgumentParser
//    val arguments = args.toList map parser.getArgument
//    var errors: List[String] = Nil
//
//    if (arguments contains Help) {
//      printUsage()
//      System.exit(0)
//    }
//
//    for (BadOption(s) <- arguments)
//      errors ::= "Unrecognised option: " + s
//
//    def getFiles(): List[File] = {
//      var files: List[File] = Nil
//      def addFile(fileName: String) {
//        val file = new File(fileName)
//        if (!file.exists)
//          errors ::= "No such file " + file
//        if (file.isDirectory)
//          errors ::= "Cannot format a directory (" + file + ")"
//        files ::= file
//      }
//      for (FileList(listName) <- arguments) {
//        val listFile = new File(listName)
//        if (!listFile.exists)
//          errors ::= "No such file: file list " + listFile
//        else if (listFile.isDirectory)
//          errors ::= "Path is a directory: file list " + listFile
//        else
//          Source.fromFile(listFile).getLines foreach addFile
//      }
//      for (FileName(fileName) <- arguments) addFile(fileName)
//      files.reverse
//    }
//
//    val files = getFiles()
//
//    val interactive = arguments contains Interactive
//    val verbose = arguments contains Verbose
//
//    if (!errors.isEmpty) {
//      errors.reverse foreach System.err.println
//      System.exit(1)
//    }
//
//    def log(s: String) = if (verbose) println(s)
//
////    if (test) {
////      var allFormattedCorrectly = true
////      def checkSource(source: Source) = {
////        val original = source.mkString
////        val formatted = ScalaFormatter.format(original, preferences)
////        formatted == original
////        // TODO: Sometimes get edits which cancel each other out
////        // val edits = ScalaFormatter.formatAsEdits(source.mkString, preferences)
////        // edits.isEmpty
////      }
////      if (files.isEmpty) {
////        val formattedCorrectly = checkSource(Source.fromInputStream(System.in))
////        allFormattedCorrectly &= formattedCorrectly
////      } else
////        for (file <- files) {
////          val formattedCorrectly = checkSource(Source.fromFile(file))
////          log("Checking " + file + " -- " + (if (formattedCorrectly) "[OK]" else "[FAILED]"))
////          allFormattedCorrectly &= formattedCorrectly
////        }
////      System.exit(if (allFormattedCorrectly) 0 else 1)
////    } else {
////      if (files.isEmpty) {
////        val original = Source.fromInputStream(System.in).mkString
////        val formatted = ScalaFormatter.format(original, preferences)
////        print(formatted)
////      } else
////        for (file <- files) {
////          val original = Source.fromFile(file).mkString
////          val formatted = ScalaFormatter.format(original, preferences)
////          if (inPlace)
////            if (formatted == original)
////              log(file + " is already correctly formatted.")
////            else {
////              log("Formatting " + file)
////              writeText(file, formatted)
////            }
////          else
////            print(formatted)
////        }
////    }
//  }
//
//  private def printUsage() {
//    println("Usage: mergedoc [options] [files...]")
//    println()
//    println("Options:")
//    println("  --help, -h                      Show help")
//    println("  --interactive, -i               Edit the input file(s) in place with a formatted version.")
//    println("  --test, -t                      Check the input(s) to see if they are correctly formatted, return a non-zero error code if not.")
//    println("  --fileList=<path>, -l=<path>    Read the list of input file(s) from a text file (one per line)")
//    println("  --verbose -v                    Verbose output")
//    println()
//  }
//
//}