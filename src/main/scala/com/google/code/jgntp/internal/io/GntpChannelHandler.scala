package com.google.code.jgntp.internal.io

import java.io._
import java.net._

import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.internal.{GntpCallbackResult, GntpErrorStatus, GntpMessageType}
import org.jboss.netty.channel._
import org.slf4j._

class GntpChannelHandler(gntpClient: NioGntpClient, listener: Option[GntpListener]) extends SimpleChannelUpstreamHandler {
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
      if (gntpClient.isRegistered) {
        handleIOError(e.getCause)
      }
      else {
        e.getCause match {
          case cause: ConnectException =>
            handleIOError(cause)
            gntpClient.retryRegistration
          case _: IOException =>
            handleMessage(new GntpOkMessage(-1, GntpMessageType.REGISTER, null))
          case cause =>
            handleIOError(cause)
        }
      }
    } finally {
      e.getChannel.close
    }
  }

  protected def handleMessage(message: GntpMessageResponse) {
    assert(message.isInstanceOf[GntpOkMessage] || message.isInstanceOf[GntpCallbackMessage] || message.isInstanceOf[GntpErrorMessage])
    logger.debug("handling message...")
    if (gntpClient.isRegistered) {
      gntpClient.notificationsSent.get(message.internalNotificationId) //TODO notificationsSent always empty ?
        .fold(logger.debug("notification is null. Not much we can do now..."))
      { case notification: GntpNotification =>
        message match {
          case _:GntpOkMessage =>
            logger.debug("OK - message.")
            try {
              listener.foreach(_.onNotificationSuccess(notification))
            } finally {
              notification.callbackTarget.foreach { callback =>
                gntpClient.notificationsSent.remove(message.internalNotificationId)
              }
            }
          case callbackMessage: GntpCallbackMessage =>
            logger.debug("Callback - message.")
            gntpClient.notificationsSent.remove(callbackMessage.internalNotificationId)
            listener.fold(throw new IllegalStateException("A GntpListener must be set in GntpClient to be able to receive callbacks")) { listener =>
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
          case errorMessage: GntpErrorMessage =>
            logger.debug("ERROR - message.")
            listener.foreach(_.onNotificationError(notification, errorMessage.status, errorMessage.description))
            if (GntpErrorStatus.UNKNOWN_APPLICATION == errorMessage.status || GntpErrorStatus.UNKNOWN_NOTIFICATION == errorMessage.status) {
              gntpClient.retryRegistration
            }
          case _ =>
            logger.warn("Unknown message type. [{}]", message)
        }
      }
    }
    else {
      logger.debug("application not registered. Not much we can do.")
      message match {
        case _: GntpOkMessage =>
          try {
            listener.foreach(_.onRegistrationSuccess)
          } finally {
            gntpClient.registrationLatch.countDown()
          }
        case errorMessage: GntpErrorMessage =>
          listener.foreach(_.onRegistrationError(errorMessage.status, errorMessage.description))
          if (GntpErrorStatus.NOT_AUTHORIZED eq errorMessage.status) {
            gntpClient.retryRegistration
          }
        case _ =>
      }
    }
  }

  protected def handleIOError(t: Throwable) {
    listener.fold(logger.error("Error in GNTP I/O operation", t))(_.onCommunicationError(t))
  }
}
