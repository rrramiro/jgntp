package com.google.code.jgntp.internal

import java.io.InputStream
import java.net.URI
import java.util.Date
import scala.language.implicitConversions

package object message {



  sealed trait HeaderValue

  case class HeaderString(value: String) extends HeaderValue
  case class HeaderNumber(value: Number) extends HeaderValue
  case class HeaderBoolean(value: Boolean) extends HeaderValue
  case class HeaderDate(value: Date) extends HeaderValue
  case class HeaderUri(value: URI) extends HeaderValue
  case class HeaderGtpnId(value: GntpId) extends HeaderValue
  case class HeaderInputStream(value: InputStream) extends HeaderValue
  case class HeaderArrayBytes(value: Array[Byte]) extends HeaderValue

  implicit def toHeaderString(field: String): HeaderValue = HeaderString(field)
  implicit def toHeaderNumber(field: Number): HeaderValue = HeaderNumber(field)
  implicit def toHeaderBoolean(field: Boolean): HeaderValue = HeaderBoolean(field)
  implicit def toHeaderDate(field: Date): HeaderValue = HeaderDate(field)
  implicit def toHeaderUri(field: URI): HeaderValue = HeaderUri(field)
  implicit def toHeaderGtpnId(field: GntpId): HeaderValue = HeaderGtpnId(field)
  implicit def toHeaderInputStream(field: InputStream): HeaderValue = HeaderInputStream(field)
  implicit def toHeaderArrayBytes(field: Array[Byte]): HeaderValue = HeaderArrayBytes(field)

}
