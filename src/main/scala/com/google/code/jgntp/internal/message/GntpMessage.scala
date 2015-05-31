package com.google.code.jgntp.internal.message

import java.io._
import java.nio.charset._
import java.util._

import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpMessageType._
import com.google.common.base._
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBufferOutputStream, ChannelBuffers}

import scala.collection.JavaConversions._
import scala.language.implicitConversions

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

class GntpMessage(val `type`: GntpMessageType)


abstract class GntpMessageRequest(`type`: GntpMessageType, password: GntpPassword) extends GntpMessage(`type`){
  val allHeaders: Seq[(String, HeaderObject)]

  def append(output: OutputStream) {
    output.write({
      s"${GntpMessage.PROTOCOL_ID}/${GntpMessage.VERSION} ${`type`.toString} ${password.getEncryptionSpec}" + (
        if (password.encrypted)
          s" ${password.keyHashAlgorithm}:${password.keyHash}.${password.salt}${GntpMessage.SEPARATOR}"
        else
          GntpMessage.SEPARATOR
        )
    }.getBytes(GntpMessage.ENCODING))
    writeHeaders(allHeaders, output)
    appendBinarySections(allHeaders, output)
  }

  def appendBinarySections(allHeaders: Seq[(String, HeaderObject)], output: OutputStream) {
    val binarySections = allHeaders.collect {
      case (key, value: BinaryHeaderValue) =>
        value.binarySection
    }
    val iter: Iterator[BinarySection] = binarySections.iterator
    while (iter.hasNext) {
      val binarySection: BinarySection = iter.next
      val data = password.encrypt(binarySection.data)
      output.write(s"${GntpMessage.BINARY_SECTION_ID} ${binarySection.id}${GntpMessage.SEPARATOR}${GntpMessage.BINARY_SECTION_LENGTH} ${data.size.toString}${GntpMessage.SEPARATOR}${GntpMessage.SEPARATOR}".getBytes(GntpMessage.ENCODING))
      output.flush()
      output.write(data)
      if (iter.hasNext) {
        output.write(s"${GntpMessage.SEPARATOR}${GntpMessage.SEPARATOR}".getBytes(GntpMessage.ENCODING))
      }
    }
  }


  def writeHeaders(allHeaders: Seq[(String, HeaderObject)], output: OutputStream): Unit ={
    val buffer: ChannelBuffer = ChannelBuffers.dynamicBuffer
    val writerTmp = new OutputStreamWriter(new ChannelBufferOutputStream(buffer), GntpMessage.ENCODING)
    allHeaders.foreach {
      case (_, HeaderSpacer) =>
        writerTmp.append(GntpMessage.SEPARATOR)
      case (name, valueInternal: HeaderObject) =>
        writerTmp.write(s"${name}${GntpMessage.HEADER_SEPARATOR} ${valueInternal.toHeader}")
        writerTmp.append(GntpMessage.SEPARATOR)
    }
    writerTmp.flush()
    val headerData: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, headerData)

    output.flush()
    output.write(password.encrypt(headerData))
    output.write(s"${GntpMessage.SEPARATOR}${GntpMessage.SEPARATOR}".getBytes(GntpMessage.ENCODING))
  }

}
