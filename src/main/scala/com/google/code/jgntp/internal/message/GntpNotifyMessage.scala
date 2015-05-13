package com.google.code.jgntp.internal.message

import java.io._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpMessageHeader
import com.google.code.jgntp.internal.message.write._
import com.google.code.jgntp.internal.GntpMessageType._
import com.google.code.jgntp.internal.GntpMessageType

class GntpNotifyMessage(notification: GntpNotification, notificationId: Long, password: GntpPassword, encrypt: Boolean) extends GntpMessage(GntpMessageType.NOTIFY, password, encrypt) {

  @throws(classOf[IOException])
  def append(output: OutputStream) {
    val writer: GntpMessageWriter = getWriter(output)
    appendStatusLine(writer)
    appendSeparator(writer)
    writer.startHeaders
    appendHeader(GntpMessageHeader.APPLICATION_NAME, notification.applicationName, writer)
    appendSeparator(writer)
    appendHeader(GntpMessageHeader.NOTIFICATION_NAME, notification.name, writer)
    appendSeparator(writer)
    appendHeader(GntpMessageHeader.NOTIFICATION_TITLE, notification.title, writer)
    appendSeparator(writer)
    if (notification.id.isDefined) {
      appendHeader(GntpMessageHeader.NOTIFICATION_ID, notification.id.get, writer)
      appendSeparator(writer)
    }
    else {
      appendHeader(GntpMessageHeader.NOTIFICATION_ID, notificationId.asInstanceOf[Number], writer)
      appendSeparator(writer)
    }
    if (notification.text.isDefined) {
      appendHeader(GntpMessageHeader.NOTIFICATION_TEXT, notification.text.get, writer)
      appendSeparator(writer)
    }
    if (notification.sticky.isDefined) {
      appendHeader(GntpMessageHeader.NOTIFICATION_STICKY, notification.sticky.get, writer)
      appendSeparator(writer)
    }
    if (notification.priority.isDefined) {
      appendHeader(GntpMessageHeader.NOTIFICATION_PRIORITY, notification.priority.get.getCode.asInstanceOf[Number], writer)
      appendSeparator(writer)
    }
    if (appendIcon(GntpMessageHeader.NOTIFICATION_ICON, notification.icon, writer)) {
      appendSeparator(writer)
    }
    if (notification.coalescingId.isDefined) {
      appendHeader(GntpMessageHeader.NOTIFICATION_COALESCING_ID, notification.coalescingId.get, writer)
      appendSeparator(writer)
    }
    notification.callbackTarget match {
      case Some(callbackTarget) =>
        appendHeader(GntpMessageHeader.NOTIFICATION_CALLBACK_TARGET, callbackTarget, writer)
      case None =>
        appendHeader(GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT, notificationId.asInstanceOf[Number], writer)
        appendSeparator(writer)
        appendHeader(GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT_TYPE, "int", writer)
    }
    appendSeparator(writer)
    appendHeader(GntpMessageHeader.NOTIFICATION_INTERNAL_ID, notificationId.asInstanceOf[Number], writer)
    appendSeparator(writer)
    import scala.collection.JavaConversions._
    for (customHeader <- notification.headers.entrySet) {
      appendHeader(customHeader.getKey, customHeader.getValue, writer)
      appendSeparator(writer)
    }
    if (!getBinarySections.isEmpty) {
      appendSeparator(writer)
    }
    writer.finishHeaders
    appendBinarySections(writer)
    clearBinarySections
  }
}