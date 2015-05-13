package com.google.code.jgntp.internal.message

import java.awt.image._
import java.io._
import java.net._
import java.nio.charset._
import java.text._
import java.util._
import javax.imageio._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpMessageHeader
import com.google.code.jgntp.internal.message.write._
import com.google.common.base._
import com.google.common.collect._
import com.google.common.io._
import com.google.code.jgntp.internal.GntpMessageType._


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
  private final val headers: java.util.Map[String, String] = Maps.newHashMap[String, String]
  private final val binarySections: java.util.List[BinarySection] = Lists.newArrayList[BinarySection]
  private final val dateFormat: DateFormat =  new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT)
  private final val buffer: StringBuilder = new StringBuilder


  @throws(classOf[IOException])
  def append(output: OutputStream)

  @throws(classOf[IOException])
  def appendStatusLine(writer: GntpMessageWriter) {
    writer.writeStatusLine(`type`)
  }

  @throws(classOf[IOException])
  def appendHeader(header: GntpMessageHeader, value: AnyRef, writer: GntpMessageWriter) {
    appendHeader(header.toString, value, writer)
  }

  @throws(classOf[IOException])
  def appendHeader(name: String, value: AnyRef, writer: GntpMessageWriter) {
    buffer.append(name).append(GntpMessage.HEADER_SEPARATOR).append(' ')
    if (value != null) {
      if (value.isInstanceOf[String]) {
        var s: String = value.asInstanceOf[String]
        s = s.replaceAll("\r\n", "\n")
        buffer.append(s)
      }
      else if (value.isInstanceOf[Number]) {
        buffer.append((value.asInstanceOf[Number]).toString)
      }
      else if (value.isInstanceOf[Boolean]) {
        var s: String = (value.asInstanceOf[Boolean]).toString
        s = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, s)
        buffer.append(s)
      }
      else if (value.isInstanceOf[Date]) {
        val s: String = dateFormat.format(value.asInstanceOf[Date])
        buffer.append(s)
      }
      else if (value.isInstanceOf[URI]) {
        buffer.append((value.asInstanceOf[URI]).toString)
      }
      else if (value.isInstanceOf[GntpId]) {
        buffer.append(value.toString)
      }
      else if (value.isInstanceOf[InputStream]) {
        val data: Array[Byte] = ByteStreams.toByteArray(value.asInstanceOf[InputStream])
        val id: GntpId = addBinary(data)
        buffer.append(id.toString)
      }
      else if (value.isInstanceOf[Array[Byte]]) {
        val data: Array[Byte] = value.asInstanceOf[Array[Byte]]
        val id: GntpId = addBinary(data)
        buffer.append(id.toString)
      }
      else {
        throw new IllegalArgumentException("Value of header [" + name + "] not supported: " + value)
      }
    }
    writer.writeHeaderLine(buffer.toString)
    buffer.setLength(0)
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
  def addBinary(data: Array[Byte]): GntpId = {
    val binarySection: BinarySection = new BinarySection(data)
    binarySections.add(binarySection)
    return GntpId.of(binarySection.id)
  }

  @throws(classOf[IOException])
  def appendBinarySections(writer: GntpMessageWriter) {
    {
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
  }

  @throws(classOf[IOException])
  def appendSeparator(writer: GntpMessageWriter) {
    writer.writeSeparator
  }

  def clearBinarySections {
    binarySections.clear
  }

  def putHeaders(map: Map[String, String]) {
    headers.putAll(map)
  }

  def getHeaders: Map[String, String] = {
    return ImmutableMap.copyOf(headers)
  }

  def getBinarySections: List[BinarySection] = {
    return ImmutableList.copyOf(binarySections: java.lang.Iterable[BinarySection])
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
    return messageWriter
  }
}
