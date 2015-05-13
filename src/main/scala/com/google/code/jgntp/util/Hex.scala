package com.google.code.jgntp.util

import com.google.common.base._

object Hex {
  private val HEX_DIGITS: Array[Char] = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

  def toHexadecimal(array: Array[Byte]): String = {
    if (array == null) {
      return null
    }
    val buffer: java.lang.StringBuffer = new java.lang.StringBuffer(array.length * 2)

    var i: Int = 0
    while (i < array.length) {
      val curByte: Int = array(i) & 0xff
      buffer.append(HEX_DIGITS(curByte >> 4))
      buffer.append(HEX_DIGITS(curByte & 0xf))
      i += 1
    }
    buffer.toString
  }

  def fromHexadecimal(s: String): Array[Byte] = {
    if (s == null) {
      return null
    }
    Preconditions.checkArgument(s.length % 2 == 0, "Invalid hex string [%s]", s)
    val result: Array[Byte] = new Array[Byte](s.length / 2)

    var i: Int = 0
    while (i < s.length) {
      val first: Int = Character.digit(s.charAt(i), 16)
      val second: Int = Character.digit(s.charAt(i + 1), 16)
      result(i / 2) = (0x0 + ((first & 0xff) << 4) + (second & 0xff)).toByte
      i = i + 2
    }

    result
  }
}
