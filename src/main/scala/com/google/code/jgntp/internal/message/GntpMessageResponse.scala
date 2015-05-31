package com.google.code.jgntp.internal.message

import java.util._

import com.google.code.jgntp.internal.GntpCallbackResult.GntpCallbackResult
import com.google.code.jgntp.internal.GntpErrorStatus.GntpErrorStatus
import com.google.code.jgntp.internal.GntpMessageType
import com.google.code.jgntp.internal.GntpMessageType._

abstract class GntpMessageResponse(`type`: GntpMessageType,
                                   val respondingType: GntpMessageType,
                                   val internalNotificationId: Long) extends GntpMessage(`type`) {

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

