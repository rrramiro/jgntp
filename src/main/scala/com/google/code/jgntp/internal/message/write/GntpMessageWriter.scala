package com.google.code.jgntp.internal.message.write

import java.io._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.internal.GntpMessageType._
import org.jboss.netty.buffer.{ChannelBufferOutputStream, ChannelBuffers, ChannelBuffer}

class GntpMessageWriter(val output: OutputStream, val password: GntpPassword) {
  protected var writer: OutputStreamWriter = new OutputStreamWriter(output, GntpMessage.ENCODING)
  val buffer: ChannelBuffer = ChannelBuffers.dynamicBuffer

  @throws(classOf[IOException])
  def writeStatusLine(`type`: GntpMessageType) = {
    s"${GntpMessage.PROTOCOL_ID}/${GntpMessage.VERSION} ${`type`.toString} ${password.getEncryptionSpec}" + (
      if (!password.encrypted)
        s":${password.iv} ${password.keyHashAlgorithm}:${password.keyHash}.${password.salt}"
      else
        ""
    )
  }

  @throws(classOf[IOException])
  def startHeaders {
    writer.flush
    //TODO fix encrypted mode
    if(password.encrypted) {
      writer = new OutputStreamWriter(new ChannelBufferOutputStream(buffer), GntpMessage.ENCODING)
    }
  }

  @throws(classOf[IOException])
  def append(line: String) {
    writer.append(line)
  }

  @throws(classOf[IOException])
  def finishHeaders {
    writer.flush
    //TODO fix encrypted mode
    if(password.encrypted) {
      val headerData: Array[Byte] = new Array[Byte](buffer.readableBytes)
      buffer.getBytes(0, headerData)
      val encryptedHeaderData: Array[Byte] = password.encrypt(headerData)
      output.write(encryptedHeaderData)
      writer = new OutputStreamWriter(output, GntpMessage.ENCODING)
      writer.append(GntpMessage.SEPARATOR).append(GntpMessage.SEPARATOR)
    }
  }

  @throws(classOf[IOException])
  def writeBinarySection(binarySection: BinarySection) {
    val data: Array[Byte] = getDataForBinarySection(binarySection)
    writer.append(s"${GntpMessage.BINARY_SECTION_ID} ${binarySection.id}${GntpMessage.SEPARATOR}${GntpMessage.BINARY_SECTION_LENGTH} ${data.size.toString}${GntpMessage.SEPARATOR}${GntpMessage.SEPARATOR}")
    writer.flush
    output.write(data)
  }

  def writeSeparator {
    writer.append(GntpMessage.SEPARATOR)
  }

  protected def getDataForBinarySection(binarySection: BinarySection): Array[Byte] = {
    password.encrypt(binarySection.data)
  }
}
