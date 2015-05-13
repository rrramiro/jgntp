package com.google.code.jgntp.internal


import com.google.common.base.{Preconditions, Predicate}
import com.google.common.collect.Maps

import scala.collection.mutable.ListBuffer

object GntpVersion {
  val ONE_DOT_ZERO  = "1.0"
}

object GntpErrorStatus {
  val values = new ListBuffer[GntpErrorStatus]


  val RESERVED = new GntpErrorStatus(100)
  val TIMED_OUT = new GntpErrorStatus(200)
  val NETWORK_FAILURE = new GntpErrorStatus(201)
  val INVALID_REQUEST = new GntpErrorStatus(300)
  val UNKNOWN_PROTOCOL = new GntpErrorStatus(301)
  val UNKNOWN_PROTOCOL_VERSION = new GntpErrorStatus(302)
  val REQUIRED_HEADER_MISSING = new GntpErrorStatus(303)
  val NOT_AUTHORIZED = new GntpErrorStatus(400)
  val UNKNOWN_APPLICATION = new GntpErrorStatus(401)
  val UNKNOWN_NOTIFICATION = new GntpErrorStatus(402)
  val INTERNAL_SERVER_ERROR = new GntpErrorStatus(500)

  def parse(s: String): GntpErrorStatus = of(s.toInt)

  def of(i: Int): GntpErrorStatus = {
    for (status <- values) {
      if (status.code == i) {
        return status
      }
    }
    null
  }
}


case class GntpErrorStatus(code: Int) {
  GntpErrorStatus.values += this
}

object GntpCallbackResult{
  val values = new ListBuffer[GntpCallbackResult]
  val CLICK = GntpCallbackResult("CLICKED", "CLICK")
  val CLOSE = GntpCallbackResult("CLOSED", "CLOSE")
  val TIMEOUT = GntpCallbackResult("TIMEDOUT", "TIMEOUT")

  def parse(s: String): GntpCallbackResult = {
    for (result <- values) {
      if (result.names.contains(s)) {
        return result
      }
    }
    return null
  }
}

case class GntpCallbackResult(namesInternal: String*){
  GntpCallbackResult.values += this
  val names = namesInternal.sorted
  def getNames = names.toArray
}

object GntpMessageHeader{
  val values = new ListBuffer[GntpMessageHeader]
  val APPLICATION_NAME = GntpMessageHeader("Application-Name")
  val APPLICATION_ICON = GntpMessageHeader("Application-Icon")
  val NOTIFICATION_COUNT = GntpMessageHeader("Notifications-Count")
  val NOTIFICATION_INTERNAL_ID = GntpMessageHeader("X-Data-Internal-Notification-ID")
  val NOTIFICATION_ID = GntpMessageHeader("Notification-ID")
  val NOTIFICATION_NAME = GntpMessageHeader("Notification-Name")
  val NOTIFICATION_DISPLAY_NAME = GntpMessageHeader("Notification-Display-Name")
  val NOTIFICATION_TITLE = GntpMessageHeader("Notification-Title")
  val NOTIFICATION_ENABLED = GntpMessageHeader("Notification-Enabled")
  val NOTIFICATION_ICON = GntpMessageHeader("Notification-Icon")
  val NOTIFICATION_TEXT = GntpMessageHeader("Notification-Text")
  val NOTIFICATION_STICKY = GntpMessageHeader("Notification-Sticky")
  val NOTIFICATION_PRIORITY = GntpMessageHeader("Notification-Priority")
  val NOTIFICATION_COALESCING_ID = GntpMessageHeader("Notification-Coalescing-ID")
  val NOTIFICATION_CALLBACK_TARGET = GntpMessageHeader("Notification-Callback-Target")
  val NOTIFICATION_CALLBACK_CONTEXT = GntpMessageHeader("Notification-Callback-Context")
  val NOTIFICATION_CALLBACK_CONTEXT_TYPE = GntpMessageHeader("Notification-Callback-Context-Type")
  val NOTIFICATION_CALLBACK_RESULT = GntpMessageHeader("Notification-Callback-Result")
  val NOTIFICATION_CALLBACK_TIMESTAMP = GntpMessageHeader("Notification-Callback-Timestamp")
  val RESPONSE_ACTION = GntpMessageHeader("Response-Action")
  val ERROR_CODE = GntpMessageHeader("Error-Code")
  val ERROR_DESCRIPTION = GntpMessageHeader("Error-Description")
}

case class GntpMessageHeader(name: String){
  GntpMessageHeader.values += this
  override def toString: String = name

  def getPredicate: Predicate[String] = new Predicate[String] {
    def apply(input: String): Boolean = {
      GntpMessageHeader.this.toString == input
    }
  }

  def getValueInMap(map: java.util.Map[String, String]): String = {
    val filteredMap: java.util.Map[String, String] = Maps.filterKeys(map, getPredicate)
    if (filteredMap.isEmpty) {
      return null
    }
    filteredMap.get(toString)
  }

  def getRequiredValueInMap(map: java.util.Map[String, String]): String = {
    val value: String = getValueInMap(map)
    Preconditions.checkNotNull(value, "Required header [%s] not found", this)
    value
  }
}

object Priority{
  val values = new ListBuffer[Priority]
  val LOWEST = Priority(-2)
  val LOW = Priority(-1)
  val NORMAL = Priority(0)
  val HIGH = Priority(1)
  val HIGHEST = Priority(2)

}

case class Priority(code: Int){
  Priority.values += this
  def getCode = code
}

object GntpMessageType extends Enumeration {
  type GntpMessageType = Value
  val REGISTER, NOTIFY, OK, CALLBACK, ERROR = Value
}


