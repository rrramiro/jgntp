package com.google.code.jgntp.internal.message

import java.io._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.GntpMessageHeader
import com.google.code.jgntp.internal.message.write._
import com.google.code.jgntp.internal.GntpMessageType

class GntpRegisterMessage(applicationInfo: GntpApplicationInfo, password: GntpPassword) extends GntpMessage(GntpMessageType.REGISTER) {

  @throws(classOf[IOException])
  def append(output: OutputStream) {

    val allHeaders: Seq[(String, HeaderObject)] = Seq(
      GntpMessageHeader.APPLICATION_NAME.toString -> (applicationInfo.name: HeaderObject)
    ) union applicationInfo.icon.map{
      case Left(uri) =>
        GntpMessageHeader.APPLICATION_ICON.toString -> (uri: HeaderObject)
      case Right(image) =>
        GntpMessageHeader.APPLICATION_ICON.toString -> (image: HeaderObject)
    }.toSeq

    val writer: GntpMessageWriter = new GntpMessageWriter(output, password)
    writer.writeStatusLine(`type`)
    writer.append(GntpMessage.SEPARATOR)
    //---------------------------------------------------
    writer.startHeaders

    allHeaders.foreach {
      case (key, value: HeaderValue) =>
        appendHeader(key, value, writer)
        writer.append(GntpMessage.SEPARATOR)
      case (key, value: BinaryHeaderValue) =>
        appendHeader(key, value, writer)
        writer.append(GntpMessage.SEPARATOR)
        binarySections += value.binarySection
    }


    appendHeader(GntpMessageHeader.NOTIFICATION_COUNT.toString, applicationInfo.notificationInfos.size, writer)
    writer.append(GntpMessage.SEPARATOR)
    writer.append(GntpMessage.SEPARATOR)
    for (notificationInfo <- applicationInfo.notificationInfos) {
      appendHeader(GntpMessageHeader.NOTIFICATION_NAME.toString, notificationInfo.name, writer)
      writer.append(GntpMessage.SEPARATOR)
      if (notificationInfo.displayName.isDefined) {
        appendHeader(GntpMessageHeader.NOTIFICATION_DISPLAY_NAME.toString, notificationInfo.displayName.get, writer)
        writer.append(GntpMessage.SEPARATOR)
      }


      notificationInfo.icon match {
        case Some(Left(uri)) =>
          appendHeader(GntpMessageHeader.NOTIFICATION_ICON.toString, uri, writer)
          writer.append(GntpMessage.SEPARATOR)
        case Some(Right(image)) =>
          val binaryHeaderValue: BinaryHeaderValue = image
          appendHeader(GntpMessageHeader.NOTIFICATION_ICON.toString, binaryHeaderValue, writer)
          binarySections += binaryHeaderValue.binarySection
          writer.append(GntpMessage.SEPARATOR)
        case None =>
      }

      appendHeader(GntpMessageHeader.NOTIFICATION_ENABLED.toString, notificationInfo.enabled, writer)
      writer.append(GntpMessage.SEPARATOR)
      writer.append(GntpMessage.SEPARATOR)
    }
    writer.finishHeaders
    //---------------------------------------------------
    appendBinarySections(writer)
    clearBinarySections
  }
}
