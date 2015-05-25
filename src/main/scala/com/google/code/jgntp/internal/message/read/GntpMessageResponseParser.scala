package com.google.code.jgntp.internal.message.read

import java.text._
import java.util.Date
import java.lang.Iterable
import com.google.code.jgntp.internal.GntpMessageHeader.GntpMessageHeader
import com.google.code.jgntp.internal.GntpMessageType
import com.google.code.jgntp.internal._
import com.google.code.jgntp.internal.message._
import com.google.common.base._
import com.google.common.collect._
import com.google.code.jgntp.internal.GntpMessageType._


import scala.util.{Failure, Try, Success}

class GntpMessageResponseParser {
  private val separatorSplitter: Splitter = Splitter.on(GntpMessage.SEPARATOR).omitEmptyStrings
  private val statusLineSplitter: Splitter = Splitter.on(' ').omitEmptyStrings.trimResults
  private val dateFormats = Seq(
    GntpMessage.DATE_TIME_FORMAT,
    GntpMessage.DATE_TIME_FORMAT_ALTERNATE,
    GntpMessage.DATE_TIME_FORMAT_GROWL_1_3
  )

  def parse(s: String): GntpMessageResponse = {
    val splitted: Iterable[String] = separatorSplitter.split(s)
    assert(!Iterables.isEmpty(splitted), "Empty message received from Growl")
    val iter: java.util.Iterator[String] = splitted.iterator
    val statusLine: String = iter.next
    assert(statusLine.startsWith(GntpMessage.PROTOCOL_ID + "/" + GntpMessage.VERSION), "Unknown protocol version")
    val statusLineIterable: Iterable[String] = statusLineSplitter.split(statusLine)
    val messageTypeText: String = Iterables.get(statusLineIterable, 1).substring(1)
    val messageType: GntpMessageType = GntpMessageType.withName(messageTypeText)
    val headers = new collection.mutable.HashMap[String, String]
    while (iter.hasNext) {
      val splittedHeader: Array[String] = iter.next.split(":", 2)
      headers.put(splittedHeader(0), splittedHeader(1).trim)
    }
    messageType match {
      case GntpMessageType.OK =>
        createOkMessage(headers.toMap)
      case GntpMessageType.CALLBACK =>
        createCallbackMessage(headers.toMap)
      case GntpMessageType.ERROR =>
        createErrorMessage(headers.toMap)
      case _ =>
        throw new IllegalStateException("Unknown response message type: " + messageType)
    }
  }

  private def createOkMessage(headers: Map[String, String]): GntpOkMessage = {
    new GntpOkMessage(
      headers.get(GntpMessageHeader.NOTIFICATION_INTERNAL_ID.toString).fold(-1L)(_.toLong),
      getRespondingType(headers),
      headers.getOrElse(GntpMessageHeader.NOTIFICATION_ID.toString, null)
    )
  }

  private def createCallbackMessage(headers: Map[String, String]): GntpCallbackMessage = {
    new GntpCallbackMessage(
      headers.get(GntpMessageHeader.NOTIFICATION_INTERNAL_ID.toString).fold(-1L)(_.toLong),
      headers.getOrElse(GntpMessageHeader.NOTIFICATION_ID.toString, null),
      GntpCallbackResult.withName(headers.getRequiredValue(GntpMessageHeader.NOTIFICATION_CALLBACK_RESULT)),
      headers.getRequiredValue(GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT),
      headers.getRequiredValue(GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT_TYPE),
      parseTimestamp(headers.getRequiredValue(GntpMessageHeader.NOTIFICATION_CALLBACK_TIMESTAMP), dateFormats)
    )
  }

  private def createErrorMessage(headers: Map[String, String]): GntpErrorMessage = {
    new GntpErrorMessage(
      headers.get(GntpMessageHeader.NOTIFICATION_INTERNAL_ID.toString).fold(-1L)(_.toLong),
      getRespondingType(headers),
      GntpErrorStatus(headers.getRequiredValue(GntpMessageHeader.ERROR_CODE).toInt),
      headers.getRequiredValue(GntpMessageHeader.ERROR_DESCRIPTION)
    )
  }

  protected def getRespondingType(headers: Map[String, String]): GntpMessageType = {
    headers.get(GntpMessageHeader.RESPONSE_ACTION.toString).fold(null.asInstanceOf[GntpMessageType])(respondingTypeName => GntpMessageType.withName(respondingTypeName))
  }

  implicit class HeaderMapWrapper(headers: Map[String, String]){
    def getRequiredValue(gntpMessageHeader: GntpMessageHeader): String = {
      headers.getOrElse(gntpMessageHeader.toString, throw new RuntimeException(s"Required header ${gntpMessageHeader.toString} not found"))
    }
  }


  private def parseTimestamp(timestampText: String, dateFormats: Seq[String]): Date = {
    dateFormats match {
      case format :: tail =>
        Try(new SimpleDateFormat(format).parse(timestampText)) match {
          case Failure(e) => parseTimestamp(timestampText, tail)
          case Success(timestamp) => timestamp
        }
      case Nil =>
        throw new RuntimeException("Timestamp Bad Format")
    }
  }
}
