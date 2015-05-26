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
    }.toSeq :+ (
      GntpMessageHeader.NOTIFICATION_COUNT.toString -> (applicationInfo.notificationInfos.size: HeaderObject)
      )

    val writer: GntpMessageWriter = new GntpMessageWriter(output, password)
    writer.append(writer.writeStatusLine(`type`))
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

    writer.append(GntpMessage.SEPARATOR)
    for (notificationInfo <- applicationInfo.notificationInfos) {
      val notificationInfoHeaders: Seq[(String, HeaderObject)] = Seq(
        GntpMessageHeader.NOTIFICATION_NAME.toString -> (notificationInfo.name: HeaderObject)
      ) union notificationInfo.displayName.map{ notificationInfoDisplayName =>
        GntpMessageHeader.NOTIFICATION_DISPLAY_NAME.toString -> (notificationInfoDisplayName: HeaderObject)
      }.toSeq union notificationInfo.icon.map{
        case Left(uri) =>
          GntpMessageHeader.NOTIFICATION_ICON.toString -> (uri: HeaderObject)
        case Right(image) =>
          GntpMessageHeader.NOTIFICATION_ICON.toString -> (image: HeaderObject)
      }.toSeq :+ (GntpMessageHeader.NOTIFICATION_ENABLED.toString -> (notificationInfo.enabled: HeaderObject))

      notificationInfoHeaders.foreach {
        case (key, value: HeaderValue) =>
          appendHeader(key, value, writer)
          writer.append(GntpMessage.SEPARATOR)
        case (key, value: BinaryHeaderValue) =>
          appendHeader(key, value, writer)
          writer.append(GntpMessage.SEPARATOR)
          binarySections += value.binarySection
      }

      writer.append(GntpMessage.SEPARATOR)
    }
    writer.finishHeaders
    //---------------------------------------------------
    appendBinarySections(writer)
    clearBinarySections
  }
}
