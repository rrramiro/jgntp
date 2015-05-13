package com.google.code.jgntp.internal.message

import java.io._
import java.util._
import com.google.code.jgntp.internal.{GntpMessageType, GntpCallbackResult, GntpErrorStatus}
import GntpMessageType._

abstract class GntpMessageResponse(val `type`: GntpMessageType,
                                   val respondingType: GntpMessageType,
                                   val internalNotificationId: Long) extends GntpMessage(`type`, null, false) {

  @throws(classOf[IOException])
  def append(output: OutputStream) {
    throw new UnsupportedOperationException("This is a response message")
  }
}

class GntpCallbackMessage(internalNotificationId: Long,
                          val notificationId: String,
                          val callbackResult: GntpCallbackResult,
                          val context: String,
                          val contextType: String,
                          val timestamp: Date) extends GntpMessageResponse(GntpMessageType.CALLBACK, GntpMessageType.NOTIFY, internalNotificationId)

class GntpOkMessage(internalNotificationId: Long,
                    respondingType: GntpMessageType,
                    val notificationId: String) extends GntpMessageResponse(GntpMessageType.OK, respondingType, internalNotificationId)


class GntpErrorMessage(internalNotificationId: Long,
                       respondingType: GntpMessageType,
                       val status: GntpErrorStatus,
                       val description: String) extends GntpMessageResponse(GntpMessageType.ERROR, respondingType, internalNotificationId)

