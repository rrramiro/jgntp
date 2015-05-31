package com.google.code.jgntp.internal.message

import com.google.code.jgntp._
import com.google.code.jgntp.internal.{GntpMessageHeader, GntpMessageType}

class GntpNotifyMessage(notification: GntpNotification, notificationId: Long, password: GntpPassword) extends GntpMessageRequest(GntpMessageType.NOTIFY, password) {
  val allHeaders: Seq[(String, HeaderObject)] = Seq(
    GntpMessageHeader.APPLICATION_NAME.toString -> (notification.applicationName: HeaderObject),
    GntpMessageHeader.NOTIFICATION_NAME.toString -> (notification.name: HeaderObject),
    GntpMessageHeader.NOTIFICATION_TITLE.toString -> (notification.title: HeaderObject)
  )  union notification.id.fold {
    GntpMessageHeader.NOTIFICATION_ID.toString -> (notificationId: HeaderObject)
  }{ notificationIdVal =>
    GntpMessageHeader.NOTIFICATION_ID.toString -> (notificationIdVal: HeaderObject)
  } +: notification.text.map{ notificationText =>
    GntpMessageHeader.NOTIFICATION_TEXT.toString -> (notificationText: HeaderObject)
  }.toSeq union notification.sticky.map{ notificationSticky =>
    GntpMessageHeader.NOTIFICATION_STICKY.toString -> (notificationSticky: HeaderObject)
  }.toSeq union notification.priority.map { notificationPriority =>
    GntpMessageHeader.NOTIFICATION_PRIORITY.toString -> (notification.priority.get.id.toString: HeaderObject)
  }.toSeq union notification.icon.map{
    case Left(uri) =>
      GntpMessageHeader.NOTIFICATION_ICON.toString -> (uri: HeaderObject)
    case Right(image) =>
      GntpMessageHeader.NOTIFICATION_ICON.toString -> (image: HeaderObject)
  }.toSeq union notification.coalescingId.map { notificationCoalescingId =>
    GntpMessageHeader.NOTIFICATION_COALESCING_ID.toString -> (notificationCoalescingId: HeaderObject)
  }.toSeq union notification.callbackTarget.fold { Seq(
    GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT.toString -> (notificationId: HeaderObject),
    GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT_TYPE.toString -> ("int": HeaderObject)
  )}{ callbackTarget =>
    Seq(GntpMessageHeader.NOTIFICATION_CALLBACK_TARGET.toString -> callbackTarget)
  } :+ (
    GntpMessageHeader.NOTIFICATION_INTERNAL_ID.toString -> (notificationId: HeaderObject)
    ) union notification.headers :+ ("" -> HeaderSpacer)

}
