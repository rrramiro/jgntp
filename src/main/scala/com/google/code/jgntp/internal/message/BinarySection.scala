package com.google.code.jgntp.internal.message

import java.security.MessageDigest

import com.google.code.jgntp.util.Hex


case class BinarySection(data: Array[Byte]) {
  lazy val id = {
    val digest = MessageDigest.getInstance(GntpMessage.BINARY_HASH_FUNCTION)
    digest.update(data)
    Hex.toHexadecimal(digest.digest)
  }
}