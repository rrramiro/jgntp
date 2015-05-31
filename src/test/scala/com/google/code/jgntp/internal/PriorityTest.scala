package com.google.code.jgntp.internal

import org.junit.Assert.assertEquals
import org.junit.Test

class PriorityTest {

  @Test
  def testGetCode(): Unit = {
    assertEquals(-2, Priority.LOWEST.id)
    assertEquals(2, Priority.HIGHEST.id)
  }

}
