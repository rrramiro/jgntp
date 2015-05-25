package com.google.code.jgntp.internal.message

import java.awt.image._
import java.io._
import java.lang.String
import java.net._
import java.nio.charset._
import java.text._
import java.util._
import javax.imageio._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpMessageHeader.GntpMessageHeader
import com.google.code.jgntp.internal.message.write._
import com.google.common.base._
import com.google.common.collect._
import com.google.common.io._
import com.google.code.jgntp.internal.GntpMessageType._
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.collection.JavaConversions._

object GntpMessage {
  val PROTOCOL_ID: String = "GNTP"
  val VERSION  = "1.0"
  val SEPARATOR: String = "\r\n"
  val HEADER_SEPARATOR: Char = ':'
  val NONE_ENCRYPTION_ALGORITHM: String = "NONE"
  val BINARY_HASH_FUNCTION: String = "MD5"
  val ENCODING: Charset = Charsets.UTF_8
  val DATE_TIME_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ssZ"
  val DATE_TIME_FORMAT_ALTERNATE: String = "yyyy-MM-dd HH:mm:ss'Z'"
  val DATE_TIME_FORMAT_GROWL_1_3: String = "yyyy-MM-dd"
  val IMAGE_FORMAT: String = "png"
  val BINARY_SECTION_ID: String = "Identifier:"
  val BINARY_SECTION_LENGTH: String = "Length:"

}

abstract class GntpMessage(val `type`: GntpMessageType) {
  var binarySections = new ListBuffer[BinarySection]

  def append(output: OutputStream)

  def appendHeader(name: String, valueInternal: HeaderObject, writer: GntpMessageWriter) {
    writer.append(s"${name}${GntpMessage.HEADER_SEPARATOR} ${valueInternal.toHeader}")
  }


  def appendBinarySections(writer: GntpMessageWriter) {
    val iter: Iterator[BinarySection] = binarySections.iterator
    while (iter.hasNext) {
      val binarySection: BinarySection = iter.next
      writer.writeBinarySection(binarySection)
      if (iter.hasNext) {
        writer.writeSeparator
        writer.writeSeparator
      }
    }
  }

  def clearBinarySections {
    binarySections.clear()// = new ListBuffer
  }

}
