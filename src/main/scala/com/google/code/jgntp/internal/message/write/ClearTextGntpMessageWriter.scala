package com.google.code.jgntp.internal.message.write

import java.io._
import com.google.code.jgntp.internal.message.BinarySection
import com.google.code.jgntp.internal.message.GntpMessage._

object ClearTextGntpMessageWriter {
  val NONE_ENCRYPTION_ALGORITHM: String = "NONE"
}

class ClearTextGntpMessageWriter extends GntpMessageWriter {
  @throws(classOf[IOException])
  protected def writeEncryptionSpec {
    writer.append(ClearTextGntpMessageWriter.NONE_ENCRYPTION_ALGORITHM)
  }

  protected def getDataForBinarySection(binarySection: BinarySection): Array[Byte] = {
    binarySection.data
  }
}

