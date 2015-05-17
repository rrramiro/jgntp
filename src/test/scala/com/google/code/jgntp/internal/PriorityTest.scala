package com.google.code.jgntp.internal

import org.junit.Test
import org.junit.Assert.assertEquals

class PriorityTest {

  @Test
  def testGetCode(): Unit = {
    assertEquals(-2, Priority.LOWEST.id)
    assertEquals(2, Priority.HIGHEST.id)
  }

}
