package com.google.code.jgntp

import java.awt.image._
import java.io._
import java.net._
import javax.imageio._
import com.google.code.jgntp.internal._
import com.google.code.jgntp.internal.message._
import org.junit._
import org.slf4j._
import com.google.common.io._
import java.util.concurrent.TimeUnit._

object GntpClientIntegrationTest {
  val APPLICATION_ICON: String = "app-icon.png"
  val RING_ICON: String = "ring.png"
  val SMS_ICON: String = "sms.png"
  val MMS_ICON: String = "mms.png"
  val BATTERY_ICON: String = "battery100.png"
  val VOICEMAIL_ICON: String = "voicemail.png"
  val PING_ICON: String = APPLICATION_ICON
  private val logger: Logger = LoggerFactory.getLogger(classOf[GntpClientIntegrationTest])
  
  class GntpListenerLog extends GntpListener{
    def onRegistrationSuccess() {
      logger.info("Registered")
    }
    def onNotificationSuccess(notification: GntpNotification) {
      logger.info("Notification success: " + notification)
    }
    def onClickCallback(notification: GntpNotification) {
      logger.info("Click callback: " + notification.context)
    }
    def onCloseCallback(notification: GntpNotification) {
      logger.info("Close callback: " + notification.context)
    }
    def onTimeoutCallback(notification: GntpNotification) {
      logger.info("Timeout callback: " + notification.context)
    }
    def onRegistrationError(status: GntpErrorStatus, description: String) {
      logger.info("Registration Error: " + status + " - desc: " + description)
    }
    def onNotificationError(notification: GntpNotification, status: GntpErrorStatus, description: String) {
      logger.info("Notification Error: " + status + " - desc: " + description)
    }
    def onCommunicationError(t: Throwable) {
      logger.error("Communication error", t)
    }
  }
}

class GntpClientIntegrationTest {
  @Test
  def test() {
    GntpClientIntegrationTest.logger.info("Running GntpClientIntegrationTest")
    val info: GntpApplicationInfo = GntpApplicationInfo("Test", Some(Right(getImage(GntpClientIntegrationTest.APPLICATION_ICON))), Seq(
      GntpNotificationInfo("Notify 1", Some(Right(getImage(GntpClientIntegrationTest.RING_ICON)))),
      GntpNotificationInfo("Notify 2", Some(Right(getImage(GntpClientIntegrationTest.SMS_ICON)))),
      GntpNotificationInfo("Notify 3", Some(Right(getImage(GntpClientIntegrationTest.MMS_ICON)))),
      GntpNotificationInfo("Notify 4", Some(Right(getImage(GntpClientIntegrationTest.BATTERY_ICON)))),
      GntpNotificationInfo("Notify 5", Some(Right(getImage(GntpClientIntegrationTest.VOICEMAIL_ICON)))),
      GntpNotificationInfo("Notify 6", Some(Right(getImage(GntpClientIntegrationTest.PING_ICON))))
    ))

    val notif1 = info.notificationInfos(0)
    val notif2 = info.notificationInfos(1)

    val client: GntpClient = GntpScala(info, growlHost = "localhost", password = GntpPassword("secret"), listener = new GntpClientIntegrationTest.GntpListenerLog)
    client.register

    client.notify(GntpNotification(
      info.name,
      notif1.name,
      "Title",
      Some("Message"),
      None,
      Map(GntpScala.APP_SPECIFIC_HEADER_PREFIX + "Filename" -> "file.txt"),
      context = Some(12345.asInstanceOf[AnyRef])
    ), 5, SECONDS)

    client.notify(GntpNotification(
      info.name,
      notif2.name,
      "Title 2",
      Some("Message 2"),
      Some(URI.create("http://slashdot.org/")),
      Map(GntpScala.CUSTOM_HEADER_PREFIX + "Payload" -> getClass.getResourceAsStream("sms.png")),

      icon = Some(Right(getImage(GntpClientIntegrationTest.APPLICATION_ICON)))
    ), 5, SECONDS)

    SECONDS.sleep(5)
    client.shutdown(5, SECONDS)



  }

  @throws(classOf[IOException])
  protected def getImage(name: String): RenderedImage = {
    val is: InputStream = getClass.getResourceAsStream(name)
    try {
      ImageIO.read(is)
    } finally {
      Closeables.closeQuietly(is)
    }
  }
}