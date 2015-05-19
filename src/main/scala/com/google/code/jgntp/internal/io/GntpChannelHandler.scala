package com.google.code.jgntp.internal.io

import java.io._
import java.net._
import com.google.code.jgntp.internal.{GntpCallbackResult, GntpErrorStatus}
import org.jboss.netty.channel._
import org.slf4j._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
import com.google.common.base._
import com.google.code.jgntp.internal.{GntpMessageType, GntpCallbackResult, GntpErrorStatus}
import GntpMessageType._

class GntpChannelHandler(gntpClient: NioGntpClient, listener: GntpListener) extends SimpleChannelUpstreamHandler {
  private val logger: Logger = LoggerFactory.getLogger(classOf[GntpChannelHandler])

  @throws(classOf[Exception])
  override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    logger.trace("Channel closed [{}]", e.getChannel)
  }

  @throws(classOf[Exception])
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val message: GntpMessageResponse = e.getMessage.asInstanceOf[GntpMessageResponse]
    handleMessage(message)
  }

  @throws(classOf[Exception])
  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    try {
      val cause: Throwable = e.getCause
      if (gntpClient.isRegistered) {
        handleIOError(cause)
      }
      else {
        if (cause.isInstanceOf[ConnectException]) {
          handleIOError(cause)
          gntpClient.retryRegistration
        }
        else if (cause.isInstanceOf[IOException]) {
          handleMessage(new GntpOkMessage(-1, GntpMessageType.REGISTER, null))
        }
        else {
          handleIOError(cause)
        }
      }
    } finally {
      e.getChannel.close
    }
  }

  protected def handleMessage(message: GntpMessageResponse) {
    Preconditions.checkState(message.isInstanceOf[GntpOkMessage] || message.isInstanceOf[GntpCallbackMessage] || message.isInstanceOf[GntpErrorMessage])
    logger.debug("handling message...")
    if (gntpClient.isRegistered) {
      val notification: GntpNotification = gntpClient.getNotificationsSent.get(message.internalNotificationId).asInstanceOf[GntpNotification]
      if (notification != null) {

        if (message.isInstanceOf[GntpOkMessage]) {
          logger.debug("OK - message.")
          try {
            if (listener != null) {
              listener.onNotificationSuccess(notification)
            }
          } finally {
            if (!notification.callbackTarget.isEmpty) {
              gntpClient.getNotificationsSent.remove(message.internalNotificationId)
            }
          }
        }
        else if (message.isInstanceOf[GntpCallbackMessage]) {
          logger.debug("Callback - message.")
          gntpClient.getNotificationsSent.remove(message.internalNotificationId)
          if (listener == null) {
            throw new IllegalStateException("A GntpListener must be set in GntpClient to be able to receive callbacks")
          }
          val callbackMessage: GntpCallbackMessage = message.asInstanceOf[GntpCallbackMessage]
          callbackMessage.callbackResult match {
            case GntpCallbackResult.CLICK | GntpCallbackResult.CLICKED =>
              listener.onClickCallback(notification)
            case GntpCallbackResult.CLOSE | GntpCallbackResult.CLOSED =>
              listener.onCloseCallback(notification)
            case GntpCallbackResult.TIMEOUT | GntpCallbackResult.TIMEDOUT =>
              listener.onTimeoutCallback(notification)
            case _ =>
              throw new IllegalStateException("Unknown callback result: " + callbackMessage.callbackResult)
          }
        }
        else if (message.isInstanceOf[GntpErrorMessage]) {
          logger.debug("ERROR - message.")
          val errorMessage: GntpErrorMessage = message.asInstanceOf[GntpErrorMessage]
          if (listener != null) {
            listener.onNotificationError(notification, errorMessage.status, errorMessage.description)
          }
          if (GntpErrorStatus.UNKNOWN_APPLICATION == errorMessage.status || GntpErrorStatus.UNKNOWN_NOTIFICATION == errorMessage.status) {
            gntpClient.retryRegistration
          }
        }
        else {
          logger.warn("Unknown message type. [{}]", message)
        }
      }
      else {
        logger.debug("notification is null. Not much we can do now...")
      }
    }
    else {
      logger.debug("application not registered. Not much we can do.")
      if (message.isInstanceOf[GntpOkMessage]) {
        try {
          if (listener != null) {
            listener.onRegistrationSuccess
          }
        } finally {
          gntpClient.registrationLatch.countDown
        }
      }
      else if (message.isInstanceOf[GntpErrorMessage]) {
        val errorMessage: GntpErrorMessage = message.asInstanceOf[GntpErrorMessage]
        if (listener != null) {
          listener.onRegistrationError(errorMessage.status, errorMessage.description)
        }
        if (GntpErrorStatus.NOT_AUTHORIZED eq errorMessage.status) {
          gntpClient.retryRegistration
        }
      }
    }
  }

  protected def handleIOError(t: Throwable) {
    if (listener == null) {
      logger.error("Error in GNTP I/O operation", t)
    }
    else {
      listener.onCommunicationError(t)
    }
  }
}
