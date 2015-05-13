package com.google.code.jgntp.internal.message

import java.security.MessageDigest

import com.google.code.jgntp.util.Hex


object BinarySection {
  val PREFIX: String = "x-growl-resource://"
}

case class BinarySection(data: Array[Byte]) {
  val id = {
    val digest = MessageDigest.getInstance(GntpMessage.BINARY_HASH_FUNCTION)
    digest.update(data)
    Hex.toHexadecimal(digest.digest)
  }

  val gntpId = BinarySection.PREFIX + id
}