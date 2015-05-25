package com.google.code.jgntp.internal

import java.awt.image.RenderedImage
import java.io.{ByteArrayOutputStream, InputStream}
import java.net.URI
import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date
import javax.imageio.ImageIO
import com.google.common.base.CaseFormat
import com.google.common.io.ByteStreams

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

  case class HeaderString(value: String) extends HeaderValue{
    override def toHeader = value.replaceAll("\r\n", "\n")
  }
  case class HeaderNumber(value: Number) extends HeaderValue{
    override def toHeader = value.toString
  }
  case class HeaderBoolean(value: Boolean) extends HeaderValue{
    override def toHeader = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, value.toString)
  }
  case class HeaderDate(value: Date) extends HeaderValue{
    override def toHeader = dateFormat.format(value)
  }
  case class HeaderUri(value: URI) extends HeaderValue{
    override def toHeader = value.toString
  }
  case class HeaderInputStream(value: InputStream) extends BinaryHeaderValue{
    val binarySection = new BinarySection(ByteStreams.toByteArray(value))
    override def toHeader = binarySection.gntpId
  }
  case class HeaderArrayBytes(value: Array[Byte]) extends BinaryHeaderValue{
    val binarySection = new BinarySection(value)
    override def toHeader = binarySection.gntpId
  }
  case class HeaderRenderedImage(value: RenderedImage) extends BinaryHeaderValue{
    private val output: ByteArrayOutputStream = new ByteArrayOutputStream
    if (!ImageIO.write(value, GntpMessage.IMAGE_FORMAT, output)) {
      throw new IllegalStateException("Could not read icon data")
    }
    val binarySection = new BinarySection(output.toByteArray)
    override def toHeader = binarySection.gntpId
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
