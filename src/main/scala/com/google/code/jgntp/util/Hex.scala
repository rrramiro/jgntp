package com.google.code.jgntp.util

import java.io.{ByteArrayOutputStream, InputStream}
import java.nio.file.Files

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.io.{BufferedSource, Source}

object Hex {

  def toHexadecimal(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }

  def fromHexadecimal(hex: String): Array[Byte] = {
    assert(hex.length % 2 == 0, s"Invalid hex string [$hex]")
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

}
