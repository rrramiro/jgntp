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

abstract class GntpMessage(`type`: GntpMessageType, password: GntpPassword, encrypt: Boolean) {
  var binarySections = new ListBuffer[BinarySection]
  private final val dateFormat: DateFormat =  new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT)
  private final val buffer: StringBuilder = new StringBuilder

  @throws(classOf[IOException])
  def append(output: OutputStream)

  @throws(classOf[IOException])
  def appendStatusLine(writer: GntpMessageWriter) {
    writer.writeStatusLine(`type`)
  }

  @throws(classOf[IOException])
  def appendHeader(name: String, valueInternal: HeaderValue, writer: GntpMessageWriter) {
    buffer.append(name).append(GntpMessage.HEADER_SEPARATOR).append(' ')

    valueInternal match {
      case HeaderNumber(value) =>
        buffer.append(value.toString)
      case HeaderUri(value) =>
        buffer.append(value.toString)
      case HeaderString(value) =>
        buffer.append(value.replaceAll("\r\n", "\n"))
      case HeaderBoolean(value) =>
        buffer.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, value.toString))
      case HeaderDate(value) =>
        buffer.append(dateFormat.format(value))
      case HeaderInputStream(value) =>
        buffer.append(addBinary(ByteStreams.toByteArray(value)))
      case HeaderArrayBytes(value) =>
        buffer.append(addBinary(value))
      case _ =>
        throw new IllegalArgumentException("Value of header [" + name + "] not supported: " + valueInternal)
    }
    writer.writeHeaderLine(buffer.toString)
    buffer.setLength(0)
  }

  @throws(classOf[IOException])
  def appendHeader(header: GntpMessageHeader, value: HeaderValue, writer: GntpMessageWriter) {
    appendHeader(header.toString, value, writer)
  }

  @throws(classOf[IOException])
  def appendIcon(header: GntpMessageHeader, icon: Option[Either[URI, RenderedImage]], writer: GntpMessageWriter): Boolean = {
    icon match {
      case None =>
        false
      case Some(Left(uri)) =>
        appendHeader(header, uri, writer)
        true
      case Some(Right(image)) =>
        val output: ByteArrayOutputStream = new ByteArrayOutputStream
        if (!ImageIO.write(image, GntpMessage.IMAGE_FORMAT, output)) {
          throw new IllegalStateException("Could not read icon data")
        }
        appendHeader(header, output.toByteArray, writer)
        true
    }
  }

  @throws(classOf[IOException])
  def addBinary(data: Array[Byte]): String = {
    val binarySection: BinarySection = new BinarySection(data)
    binarySections += binarySection
    binarySection.gntpId
  }

  @throws(classOf[IOException])
  def appendBinarySections(writer: GntpMessageWriter) {
    val iter: Iterator[BinarySection] = binarySections.iterator
    while (iter.hasNext) {
      val binarySection: BinarySection = iter.next
      writer.writeBinarySection(binarySection)
      if (iter.hasNext) {
        appendSeparator(writer)
        appendSeparator(writer)
      }
    }
  }

  @throws(classOf[IOException])
  def appendSeparator(writer: GntpMessageWriter) {
    writer.writeSeparator
  }

  def clearBinarySections {
    binarySections = new ListBuffer
  }

  protected def getWriter(output: OutputStream): GntpMessageWriter = {
    var messageWriter: GntpMessageWriter = null
    if (encrypt) {
      messageWriter = new EncryptedGntpMessageWriter
    }
    else {
      messageWriter = new ClearTextGntpMessageWriter
    }
    messageWriter.prepare(output, password)
    messageWriter
  }
}
