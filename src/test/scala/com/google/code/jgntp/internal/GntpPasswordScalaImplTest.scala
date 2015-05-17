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
    assertEquals(password.keyHash, "8dea4bdb68ffd8a3d7a5a715acf4092b8a419d43889d5c0898b48bae7cd000854b0966dcf1a1d6bf607727ddf2b4b5dc094b59778bddc0aaaa9d70879a3674ed")
  }
}