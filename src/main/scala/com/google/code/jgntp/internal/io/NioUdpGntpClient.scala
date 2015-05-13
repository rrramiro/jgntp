package com.google.code.jgntp.internal.io

import java.net._
import java.util.concurrent._
import org.jboss.netty.bootstrap._
import org.jboss.netty.channel.socket._
import org.jboss.netty.channel.socket.oio._
import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
import com.google.common.base._
import com.google.common.collect._

object NioUdpGntpClient {
  private val notificationsSent: BiMap[Long, AnyRef] = HashBiMap.create[Long, AnyRef]
}

class NioUdpGntpClient(applicationInfo: GntpApplicationInfo, growlAddress: SocketAddress, executor: Executor, password: GntpPassword, encrypted: Boolean) extends NioGntpClient(applicationInfo, growlAddress, password, encrypted) {
  assert(executor != null, "Executor must not be null")
  private final val bootstrap: ConnectionlessBootstrap = new ConnectionlessBootstrap(new OioDatagramChannelFactory(executor))
  bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, null)))
  bootstrap.setOption("broadcast", "false")

  private final val datagramChannel: DatagramChannel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]


  protected def doRegister {
    val message: GntpMessage = new GntpRegisterMessage(getApplicationInfo, getPassword, isEncrypted)
    datagramChannel.write(message, getGrowlAddress)
    getRegistrationLatch.countDown
  }

  protected def doNotify(notification: GntpNotification) {
    val message: GntpNotifyMessage = new GntpNotifyMessage(notification, -1, getPassword, isEncrypted)
    datagramChannel.write(message, getGrowlAddress)
  }

  @throws(classOf[InterruptedException])
  protected def doShutdown(timeout: Long, unit: TimeUnit) {
    datagramChannel.close.await(timeout, unit)
    bootstrap.releaseExternalResources
  }

  private[io] def getNotificationsSent: BiMap[Long, AnyRef] = NioUdpGntpClient.notificationsSent

  private[io] def retryRegistration {}
}
