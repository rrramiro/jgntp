package com.google.code.jgntp.internal.message.read

import java.text._
import java.util.{Map, Iterator, Date}
import java.lang.Iterable
import com.google.code.jgntp.internal.{GntpVersion, GntpMessageHeader, GntpCallbackResult, GntpErrorStatus}
import com.google.code.jgntp.internal.message._
import com.google.common.base._
import com.google.common.collect._
import com.google.code.jgntp.internal.GntpMessageType._
import com.google.code.jgntp.internal.GntpMessageType._
import com.google.code.jgntp.internal.GntpMessageType

class GntpMessageResponseParser {
  private val separatorSplitter: Splitter = Splitter.on(GntpMessage.SEPARATOR).omitEmptyStrings
  private val statusLineSplitter: Splitter = Splitter.on(' ').omitEmptyStrings.trimResults

  def parse(s: String): GntpMessageResponse = {
    val splitted: Iterable[String] = separatorSplitter.split(s)
    assert(!Iterables.isEmpty(splitted), "Empty message received from Growl")
    val iter: java.util.Iterator[String] = splitted.iterator
    val statusLine: String = iter.next
    assert(statusLine.startsWith(GntpMessage.PROTOCOL_ID + "/" + GntpVersion.ONE_DOT_ZERO), "Unknown protocol version")
    val statusLineIterable: Iterable[String] = statusLineSplitter.split(statusLine)
    val messageTypeText: String = Iterables.get(statusLineIterable, 1).substring(1)
    val messageType: GntpMessageType = GntpMessageType.values.find( (msg : GntpMessageType) => msg.toString == messageTypeText).orNull
    val headers: java.util.Map[String, String] = Maps.newHashMap[String, String]
    while (iter.hasNext) {
      val splittedHeader: Array[String] = iter.next.split(":", 2)
      headers.put(splittedHeader(0), splittedHeader(1).trim)
    }
    messageType match {
      case GntpMessageType.OK =>
        createOkMessage(headers)
      case GntpMessageType.CALLBACK =>
        createCallbackMessage(headers)
      case GntpMessageType.ERROR =>
        createErrorMessage(headers)
      case _ =>
        throw new IllegalStateException("Unknown response message type: " + messageType)
    }
  }

  protected def createOkMessage(headers: java.util.Map[String, String]): GntpOkMessage = {
    val notificationId: String = GntpMessageHeader.NOTIFICATION_ID.getValueInMap(headers)
    new GntpOkMessage(getInternalNotificationId(headers), getRespondingType(headers), notificationId)
  }

  protected def createCallbackMessage(headers: java.util.Map[String, String]): GntpCallbackMessage = {
    val notificationId: String = GntpMessageHeader.NOTIFICATION_ID.getValueInMap(headers)
    val callbackResultText: String = GntpMessageHeader.NOTIFICATION_CALLBACK_RESULT.getRequiredValueInMap(headers)
    val callbackResult: GntpCallbackResult = GntpCallbackResult.parse(callbackResultText)
    val context: String = GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT.getRequiredValueInMap(headers)
    val contextType: String = GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT_TYPE.getRequiredValueInMap(headers)
    val timestampText: String = GntpMessageHeader.NOTIFICATION_CALLBACK_TIMESTAMP.getRequiredValueInMap(headers)
    var timestamp: Date = null
    try {
      timestamp = new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT).parse(timestampText)
    }
    catch {
      case e: ParseException => {
        try {
          timestamp = new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT_ALTERNATE).parse(timestampText)
        }
        catch {
          case e1: ParseException => {
            try {
              timestamp = new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT_GROWL_1_3).parse(timestampText)
            }
            catch {
              case e2: ParseException => {
                throw new RuntimeException(e)
              }
            }
          }
        }
      }
    }
    new GntpCallbackMessage(getInternalNotificationId(headers), notificationId, callbackResult, context, contextType, timestamp)
  }

  protected def createErrorMessage(headers: Map[String, String]): GntpErrorMessage = {
    val code: String = GntpMessageHeader.ERROR_CODE.getRequiredValueInMap(headers)
    val description: String = GntpMessageHeader.ERROR_DESCRIPTION.getRequiredValueInMap(headers)
    val errorStatus: GntpErrorStatus = GntpErrorStatus.parse(code)
    new GntpErrorMessage(getInternalNotificationId(headers), getRespondingType(headers), errorStatus, description)
  }

  protected def getInternalNotificationId(headers: Map[String, String]): Long = {
    val value: String = GntpMessageHeader.NOTIFICATION_INTERNAL_ID.getValueInMap(headers)
    if (value == null) -1 else value.toLong
  }

  protected def getRespondingType(headers: Map[String, String]): GntpMessageType = {
    val respondingTypeName: String = GntpMessageHeader.RESPONSE_ACTION.getValueInMap(headers)
    if (respondingTypeName == null) null else GntpMessageType.values.find( (msg : GntpMessageType) => msg.toString == respondingTypeName).orNull
  }
}
