package com.google.code.jgntp.internal.message.write

import java.io._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpVersion
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.util.Hex
import com.google.code.jgntp.internal.GntpMessageType._

abstract class GntpMessageWriter {
  protected var output: OutputStream = null
  protected var writer: OutputStreamWriter = null
  protected var password: GntpPassword = null

  def prepare(outputStream: OutputStream, gntpPassword: GntpPassword) {
    this.output = outputStream
    this.writer = new OutputStreamWriter(outputStream, GntpMessage.ENCODING)
    this.password = gntpPassword
  }

  @throws(classOf[IOException])
  def writeStatusLine(`type`: GntpMessageType) {
    writer.append(GntpMessage.PROTOCOL_ID).append('/').append(GntpVersion.ONE_DOT_ZERO)
    writer.append(' ').append(`type`.toString)
    writer.append(' ')
    writeEncryptionSpec
    if (password != null) {
      writer.append(' ').append(password.keyHashAlgorithm)
      writer.append(':').append(password.keyHash)
      writer.append('.').append(password.salt)
    }
  }

  @throws(classOf[IOException])
  def startHeaders {
    writer.flush
  }

  @throws(classOf[IOException])
  def writeHeaderLine(line: String) {
    writer.append(line)
  }

  @throws(classOf[IOException])
  def finishHeaders {
    writer.flush
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
  protected def writeEncryptionSpec

  protected def getDataForBinarySection(binarySection: BinarySection): Array[Byte]
}
