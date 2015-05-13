package com.google.code.jgntp.internal

import org.junit._
import com.google.code.jgntp._
import org.junit.Assert._

class GntpPasswordScalaImplTest {
  @Test
  def testKeyGeneration() {
    val password: GntpPassword = new GntpPassword("test") {
     override protected def getSeed: Long = 10000000L
    }
    val expectedKey = "8DEA4BDB68FFD8A3D7A5A715ACF4092B8A419D43889D5C0898B48BAE7CD000854B0966DCF1A1D6BF607727DDF2B4B5DC094B59778BDDC0AAAA9D70879A3674ED"
    assertEquals(password.keyHash, expectedKey)
  }
}