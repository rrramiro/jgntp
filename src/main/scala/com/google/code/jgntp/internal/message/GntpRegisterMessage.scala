package com.google.code.jgntp.internal.message

import java.io._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpMessageHeader
import com.google.code.jgntp.internal.message.write._
import com.google.code.jgntp.internal.GntpMessageType

class GntpRegisterMessage(applicationInfo: GntpApplicationInfo, password: GntpPassword) extends GntpMessage(GntpMessageType.REGISTER) {

  @throws(classOf[IOException])
  def append(output: OutputStream) {
    val writer: GntpMessageWriter = new GntpMessageWriter(output, password)
    appendStatusLine(writer)
    appendSeparator(writer)
    writer.startHeaders
    appendHeader(GntpMessageHeader.APPLICATION_NAME, applicationInfo.name, writer)
    appendSeparator(writer)
    if (appendIcon(GntpMessageHeader.APPLICATION_ICON, applicationInfo.icon, writer)) {
      appendSeparator(writer)
    }
    appendHeader(GntpMessageHeader.NOTIFICATION_COUNT, applicationInfo.notificationInfos.size, writer)
    appendSeparator(writer)
    appendSeparator(writer)
    for (notificationInfo <- applicationInfo.notificationInfos) {
      appendHeader(GntpMessageHeader.NOTIFICATION_NAME, notificationInfo.name, writer)
      appendSeparator(writer)
      if (notificationInfo.displayName.isDefined) {
        appendHeader(GntpMessageHeader.NOTIFICATION_DISPLAY_NAME, notificationInfo.displayName.get, writer)
        appendSeparator(writer)
      }
      if (appendIcon(GntpMessageHeader.NOTIFICATION_ICON, notificationInfo.icon, writer)) {
        appendSeparator(writer)
      }
      appendHeader(GntpMessageHeader.NOTIFICATION_ENABLED, notificationInfo.enabled, writer)
      appendSeparator(writer)
      appendSeparator(writer)
    }
    writer.finishHeaders
    appendBinarySections(writer)
    clearBinarySections
  }
}
