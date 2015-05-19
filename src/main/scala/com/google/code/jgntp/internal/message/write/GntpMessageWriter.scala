package com.google.code.jgntp.internal.message.write

import java.io._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpVersion
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.util.Hex
import com.google.code.jgntp.internal.GntpMessageType._
import org.jboss.netty.buffer.{ChannelBufferOutputStream, ChannelBuffers, ChannelBuffer}

class GntpMessageWriter(val output: OutputStream, val password: GntpPassword) {
  protected var writer: OutputStreamWriter = new OutputStreamWriter(output, GntpMessage.ENCODING)
  val buffer: ChannelBuffer = ChannelBuffers.dynamicBuffer

  @throws(classOf[IOException])
  def writeStatusLine(`type`: GntpMessageType) {
    writer.append(GntpMessage.PROTOCOL_ID).append('/').append(GntpVersion.ONE_DOT_ZERO)
    writer.append(' ').append(`type`.toString)
    writer.append(' ')
    writeEncryptionSpec
    if (!password.textPassword.isEmpty) {
      writer.append(' ').append(password.keyHashAlgorithm)
      writer.append(':').append(password.keyHash)
      writer.append('.').append(password.salt)
    }
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
  def writeHeaderLine(line: String) {
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
      writeSeparator
      writeSeparator
    }
  }

  @throws(classOf[IOException])
  def writeBinarySection(binarySection: BinarySection) {
    val data: Array[Byte] = getDataForBinarySection(binarySection)
    writer.append(GntpMessage.BINARY_SECTION_ID).append(' ').append(binarySection.id)
    writeSeparator
    writer.append(GntpMessage.BINARY_SECTION_LENGTH).append(' ').append(data.size.toString)
    writeSeparator
    writeSeparator
    writer.flush
    output.write(data)
  }

  @throws(classOf[IOException])
  def writeSeparator {
    writer.append(GntpMessage.SEPARATOR)
  }

  @throws(classOf[IOException])
  protected def writeEncryptionSpec {
    writer.append(password.getEncryptionSpec)
  }

  protected def getDataForBinarySection(binarySection: BinarySection): Array[Byte] = {
    password.encrypt(binarySection.data)
  }
}
