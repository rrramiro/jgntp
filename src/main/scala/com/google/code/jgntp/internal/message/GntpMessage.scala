package com.google.code.jgntp.internal.message

import java.nio.charset._
import java.security.MessageDigest

import com.google.code.jgntp.internal.GntpMessageType._
import com.google.code.jgntp.util.Hex

object GntpMessage {
  val PROTOCOL_ID: String = "GNTP"
  val VERSION  = "1.0"
  val SEPARATOR: String = "\r\n"
  val HEADER_SEPARATOR: Char = ':'
  val NONE_ENCRYPTION_ALGORITHM: String = "NONE"
  val BINARY_HASH_FUNCTION: String = "MD5"
  val ENCODING: Charset = StandardCharsets.UTF_8
  val DATE_TIME_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ssZ"
  val DATE_TIME_FORMAT_ALTERNATE: String = "yyyy-MM-dd HH:mm:ss'Z'"
  val DATE_TIME_FORMAT_GROWL_1_3: String = "yyyy-MM-dd"
  val IMAGE_FORMAT: String = "png"
  val BINARY_SECTION_ID: String = "Identifier:"
  val BINARY_SECTION_LENGTH: String = "Length:"
  val BINARY_SECTION_PREFIX: String = "x-growl-resource://"
}

class GntpMessage(val `type`: GntpMessageType)

case class BinarySection(data: Array[Byte]) {
  //TODO Move to GntpSecurity
  val id = {
    val digest = MessageDigest.getInstance(GntpMessage.BINARY_HASH_FUNCTION)
    digest.update(data)
    Hex.toHexadecimal(digest.digest)
  }

  val gntpId = GntpMessage.BINARY_SECTION_PREFIX + id
}


