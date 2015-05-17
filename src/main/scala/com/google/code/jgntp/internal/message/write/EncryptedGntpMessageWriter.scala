package com.google.code.jgntp.internal.message.write

import java.io._
import java.security._
import javax.crypto._
import javax.crypto.spec._
import com.google.code.jgntp.util.Hex
import org.jboss.netty.buffer._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._

object EncryptedGntpMessageWriter {
  val DEFAULT_ALGORITHM: String = "DES"
  val DEFAULT_TRANSFORMATION: String = "DES/CBC/PKCS5Padding"
}

class EncryptedGntpMessageWriter extends GntpMessageWriter {
  private var cipher: Cipher = null
  private var secretKey: SecretKey = null
  private var iv: IvParameterSpec = null
  private var buffer: ChannelBuffer = null

  override def prepare(outputStream: OutputStream, gntpPassword: GntpPassword) {
    super.prepare(outputStream, gntpPassword)
    buffer = ChannelBuffers.dynamicBuffer
    try {
      val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(EncryptedGntpMessageWriter.DEFAULT_ALGORITHM)
      secretKey = keyFactory.generateSecret(gntpPassword.keySpec)
      iv = new IvParameterSpec(secretKey.getEncoded)
      cipher = Cipher.getInstance(EncryptedGntpMessageWriter.DEFAULT_TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
    }
    catch {
      case e: GeneralSecurityException => {
        throw new RuntimeException(e)
      }
    }
  }

  @throws(classOf[IOException])
  protected def writeEncryptionSpec {
    writer.append(EncryptedGntpMessageWriter.DEFAULT_ALGORITHM).append(':').append(Hex.toHexadecimal(iv.getIV))
  }

  @throws(classOf[IOException])
  override def startHeaders {
    super.startHeaders
    writer = new OutputStreamWriter(new ChannelBufferOutputStream(buffer), GntpMessage.ENCODING)
  }

  @throws(classOf[IOException])
  override def finishHeaders {
    super.finishHeaders
    val headerData: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, headerData)
    val encryptedHeaderData: Array[Byte] = encrypt(headerData)
    output.write(encryptedHeaderData)
    writer = new OutputStreamWriter(output, GntpMessage.ENCODING)
    writeSeparator
    writeSeparator
  }

  protected def getDataForBinarySection(binarySection: BinarySection): Array[Byte] = {
    encrypt(binarySection.data)
  }

  protected def encrypt(data: Array[Byte]): Array[Byte] = {
    try {
      cipher.doFinal(data)
    }
    catch {
      case e: GeneralSecurityException => {
        throw new RuntimeException(e)
      }
    }
  }
}

