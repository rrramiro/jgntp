package com.google.code.jgntp.internal.message

import com.google.code.jgntp._
import com.google.code.jgntp.internal.{GntpMessageHeader, GntpMessageType}

class GntpRegisterMessage(applicationInfo: GntpApplicationInfo, password: GntpPassword) extends GntpMessageRequest(GntpMessageType.REGISTER, password) {
  val allHeaders: Seq[(String, HeaderObject)] = Seq(
    GntpMessageHeader.APPLICATION_NAME.toString -> (applicationInfo.name: HeaderObject)
  ) union applicationInfo.icon.map{
    case Left(uri) =>
      GntpMessageHeader.APPLICATION_ICON.toString -> (uri: HeaderObject)
    case Right(image) =>
      GntpMessageHeader.APPLICATION_ICON.toString -> (image: HeaderObject)
  }.toSeq union Seq(
    GntpMessageHeader.NOTIFICATION_COUNT.toString -> (applicationInfo.notificationInfos.size: HeaderObject),
    "" -> HeaderSpacer
  ) union (for (notificationInfo <- applicationInfo.notificationInfos) yield {
    Seq(
      GntpMessageHeader.NOTIFICATION_NAME.toString -> (notificationInfo.name: HeaderObject)
    ) union notificationInfo.displayName.map{ notificationInfoDisplayName =>
      GntpMessageHeader.NOTIFICATION_DISPLAY_NAME.toString -> (notificationInfoDisplayName: HeaderObject)
    }.toSeq union notificationInfo.icon.map{
      case Left(uri) =>
        GntpMessageHeader.NOTIFICATION_ICON.toString -> (uri: HeaderObject)
      case Right(image) =>
        GntpMessageHeader.NOTIFICATION_ICON.toString -> (image: HeaderObject)
    }.toSeq union Seq(
      GntpMessageHeader.NOTIFICATION_ENABLED.toString -> (notificationInfo.enabled: HeaderObject),
      "" -> HeaderSpacer
    )
  }).flatten

}
