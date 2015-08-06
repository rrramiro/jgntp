package com.google.code.jgntp.internal.message

import java.util._

import com.google.code.jgntp.internal.GntpCallbackResult.GntpCallbackResult
import com.google.code.jgntp.internal.GntpErrorStatus.GntpErrorStatus
import com.google.code.jgntp.internal.GntpMessageType
import com.google.code.jgntp.internal.GntpMessageType._

abstract class GntpMessageResponse(val `type`: GntpMessageType,
                                   val respondingType: GntpMessageType,
                                   val internalNotificationId: Option[Long]) extends GntpMessage {

}

class GntpCallbackMessage(internalNotificationId: Option[Long],
                          val notificationId: Option[String],
                          val callbackResult: GntpCallbackResult,
                          val context: String,
                          val contextType: String,
                          val timestamp: Date) extends GntpMessageResponse(GntpMessageType.CALLBACK, GntpMessageType.NOTIFY, internalNotificationId)

class GntpOkMessage(internalNotificationId: Option[Long],
                    respondingType: GntpMessageType,
                    val notificationId: Option[String]) extends GntpMessageResponse(GntpMessageType.OK, respondingType, internalNotificationId)


class GntpErrorMessage(internalNotificationId: Option[Long],
                       respondingType: GntpMessageType,
                       val status: GntpErrorStatus,
                       val description: String) extends GntpMessageResponse(GntpMessageType.ERROR, respondingType, internalNotificationId)

