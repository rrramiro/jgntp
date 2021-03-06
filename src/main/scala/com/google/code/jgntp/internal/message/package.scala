package com.google.code.jgntp.internal

import java.awt.image.RenderedImage
import java.io.{FileInputStream, BufferedInputStream, ByteArrayOutputStream, InputStream}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}
import java.security.MessageDigest
import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date
import javax.imageio.ImageIO

import com.google.code.jgntp.util.Hex

import scala.collection.mutable.ListBuffer
import scala.io.{Codec, Source}
import scala.language.implicitConversions

package object message {

  private final val dateFormat: DateFormat =  new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT)

  sealed trait HeaderObject{
    def toHeader: String
  }

  sealed trait HeaderValue extends HeaderObject

  trait BinaryHeaderValue extends HeaderObject {
    def binarySection: BinarySection
  }

  object HeaderSpacer extends HeaderObject{
    def toHeader: String = GntpMessage.SEPARATOR
  }

  case class HeaderString(value: String) extends HeaderValue{
    override def toHeader = value.replaceAll("\r\n", "\n")
  }
  case class HeaderNumber(value: Number) extends HeaderValue{
    override def toHeader = value.toString
  }
  case class HeaderBoolean(value: Boolean) extends HeaderValue{
    override def toHeader = value.toString.toLowerCase.capitalize
  }
  case class HeaderDate(value: Date) extends HeaderValue{
    override def toHeader = dateFormat.format(value)
  }
  case class HeaderUri(value: URI) extends HeaderValue{
    override def toHeader = value.toString
  }
  case class HeaderInputStream(value: InputStream) extends BinaryHeaderValue{
    val binarySection = new BinarySection(Stream.continually(value.read)
      .takeWhile(_ != -1)
      .map(_.toByte).toArray)
    value.close() //TODO check

    override def toHeader = GntpMessage.BINARY_SECTION_PREFIX + binarySection.id
  }
  case class HeaderArrayBytes(value: Array[Byte]) extends BinaryHeaderValue{
    val binarySection = new BinarySection(value)
    override def toHeader = GntpMessage.BINARY_SECTION_PREFIX + binarySection.id
  }
  case class HeaderRenderedImage(value: RenderedImage) extends BinaryHeaderValue{
    private val output: ByteArrayOutputStream = new ByteArrayOutputStream
    if (!ImageIO.write(value, GntpMessage.IMAGE_FORMAT, output)) {
      throw new IllegalStateException("Could not read icon data")
    }
    val binarySection = new BinarySection(output.toByteArray)
    override def toHeader = GntpMessage.BINARY_SECTION_PREFIX + binarySection.id
  }

  implicit def toHeaderString(field: String): HeaderValue = HeaderString(field)
  implicit def toHeaderNumber(field: Number): HeaderValue = HeaderNumber(field)
  implicit def toHeaderLong(field: Long): HeaderValue = HeaderNumber(field)
  implicit def toHeaderInt(field: Int): HeaderValue = HeaderNumber(field)
  implicit def toHeaderBoolean(field: Boolean): HeaderValue = HeaderBoolean(field)
  implicit def toHeaderDate(field: Date): HeaderValue = HeaderDate(field)
  implicit def toHeaderUri(field: URI): HeaderValue = HeaderUri(field)
  implicit def toHeaderInputStream(field: InputStream): BinaryHeaderValue = HeaderInputStream(field)
  implicit def toHeaderArrayBytes(field: Array[Byte]): BinaryHeaderValue = HeaderArrayBytes(field)
  implicit def toHeaderRenderedImage(field: RenderedImage): BinaryHeaderValue = HeaderRenderedImage(field)

}
