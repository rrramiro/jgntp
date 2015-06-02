package com.google.code.jgntp.internal.io

import java.net._
import java.util.concurrent._

import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
import com.google.common.collect._
import org.jboss.netty.bootstrap._
import org.jboss.netty.channel.socket._
import org.jboss.netty.channel.socket.oio._

import scala.collection.mutable

object NioUdpGntpClient {
  private val notificationsSent: mutable.Map[Long, AnyRef] = new mutable.HashMap[Long, AnyRef]
}

class NioUdpGntpClient(applicationInfo: GntpApplicationInfo, growlAddress: SocketAddress, executor: Executor, password: GntpPassword) extends NioGntpClient(applicationInfo, growlAddress, password) {
  assert(executor != null, "Executor must not be null")
  private final val bootstrap: ConnectionlessBootstrap = new ConnectionlessBootstrap(new OioDatagramChannelFactory(executor))
  bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, None)))
  bootstrap.setOption("broadcast", "false")

  private final val datagramChannel: DatagramChannel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]


  protected def doRegister {
    val message: GntpMessage = new GntpRegisterMessage(applicationInfo, password)
    datagramChannel.write(message, growlAddress)
    registrationLatch.countDown
  }

  protected def doNotify(notification: GntpNotification) {
    val message: GntpNotifyMessage = new GntpNotifyMessage(notification, -1, password)
    datagramChannel.write(message, growlAddress)
  }

  @throws(classOf[InterruptedException])
  protected def doShutdown(timeout: Long, unit: TimeUnit) {
    datagramChannel.close.await(timeout, unit)
    bootstrap.releaseExternalResources
  }

  val notificationsSent: mutable.Map[Long, AnyRef] = NioUdpGntpClient.notificationsSent

  def retryRegistration {}
}
