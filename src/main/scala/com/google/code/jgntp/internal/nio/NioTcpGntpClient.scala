package com.google.code.jgntp.internal.nio

import java.net._
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit, Executor}
import com.google.code.jgntp.internal.io.NioGntpClient
import io.netty.channel.nio.{NioEventLoopGroup, NioEventLoop}
import io.netty.util.concurrent.{GlobalEventExecutor, EventExecutor}
import io.netty.channel.socket.nio.NioSocketChannel
import scala.collection.concurrent.{TrieMap, Map}

import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
//import org.jboss.netty.bootstrap._
//import org.jboss.netty.channel._
//import org.jboss.netty.channel.group._
//import org.jboss.netty.channel.socket.nio._

import org.slf4j._


import io.netty.bootstrap._
import io.netty.channel._
import io.netty.channel.group._
import io.netty.channel.socket.nio._
import scala.collection.mutable

object NioTcpGntpClient {
  private val logger: Logger = LoggerFactory.getLogger(classOf[NioTcpGntpClient])
}

class NioTcpGntpClient(applicationInfo: GntpApplicationInfo,
                       growlAddress: SocketAddress,
                       executor: Executor,
                       listener: GntpListener,
                       password: GntpPassword,
                       retryTime: Long = 0L,
                       retryTimeUnit: TimeUnit,
                       notificationRetryCount: Int = 0) extends NioGntpClient(applicationInfo, growlAddress, password) {

  assert(executor != null, "Executor must not be null")
  if (retryTime > 0) {
    assert(retryTimeUnit != null, "Retry time unit must not be null")
  }
  assert(notificationRetryCount >= 0, "Notification retries must be equal or greater than zero")

  //private final val bootstrap: ClientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor))
  val group = new NioEventLoopGroup()
  private final val bootstrap: Bootstrap = new Bootstrap()
  bootstrap.group(group)
  bootstrap.remoteAddress(growlAddress)
  bootstrap.option(ChannelOption.TCP_NODELAY, true.asInstanceOf[java.lang.Boolean])
  bootstrap.option(ChannelOption.SO_TIMEOUT, new Integer(60 * 1000))
  bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, new DefaultMessageSizeEstimator(1024) )
  bootstrap.channel(classOf[NioSocketChannel])
  bootstrap.handler(new GntpChannelPipelineFactory(new GntpChannelHandler(this, Option(listener))))
  //bootstrap.setOption("tcpNoDelay", true)
  //bootstrap.setOption("remoteAddress", growlAddress)
  //bootstrap.setOption("soTimeout", 60 * 1000)
  //bootstrap.setOption("receiveBufferSizePredictor", new AdaptiveReceiveBufferSizePredictor)
  private final val retryExecutorService: ScheduledExecutorService = if (retryTime > 0) Executors.newSingleThreadScheduledExecutor else null
  private final val channelGroup: ChannelGroup = new DefaultChannelGroup("jgntp", GlobalEventExecutor.INSTANCE)

  @volatile
  private var tryingRegistration: Boolean = false
  private final val notificationIdGenerator: AtomicLong = new AtomicLong
  val notificationRetries: Map[GntpNotification, Integer] = new TrieMap[GntpNotification, Integer]


  protected def doRegister {
    bootstrap.connect.addListener(new ChannelFutureListener {
      @throws(classOf[Exception])
      def operationComplete(future: ChannelFuture) {
        tryingRegistration = false
        if (future.isSuccess) {
          channelGroup.add(future.channel())
          val message: GntpMessage = new GntpRegisterMessage(applicationInfo, password)
          future.channel().write(message)
        }
      }
    })
  }

  protected def doNotify(notification: GntpNotification) {
    bootstrap.connect.addListener(new ChannelFutureListener {
      @throws(classOf[Exception])
      def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          channelGroup.add(future.channel)
          val notificationId: Long = notificationIdGenerator.getAndIncrement
          notificationsSent.put(notificationId, notification)
          val message: GntpMessage = new GntpNotifyMessage(notification, notificationId, password)
          future.channel.write(message)
        }
        else {
          if (retryExecutorService != null) {
            var count: Integer = notificationRetries(notification)
            if (count == null) {
              count = 1
            }
            if (count <= notificationRetryCount) {
              NioTcpGntpClient.logger.debug("Failed to send notification [{}], retry [{}/{}] in [{}-{}]", Array(notification, count, notificationRetryCount, retryTime, retryTimeUnit))
              notificationRetries.put(notification, ({
                count += 1; count
              }))
              retryExecutorService.schedule(new Runnable {
                override def run {
                  NioTcpGntpClient.this.notify(notification)
                }
              }, retryTime, retryTimeUnit)
            }
            else {
              NioTcpGntpClient.logger.debug("Failed to send notification [{}], giving up", notification)
              notificationRetries.remove(notification)
            }
          }
          notificationsSent.find(_._2 == notification).foreach( entity =>
            notificationsSent.remove(entity._1)
          )
        }
      }
    })
  }

  @throws(classOf[InterruptedException])
  protected def doShutdown(timeout: Long, unit: TimeUnit) {
    if (retryExecutorService != null) {
      retryExecutorService.shutdownNow
      retryExecutorService.awaitTermination(timeout, unit)
    }
    channelGroup.close.await(timeout, unit)
    //bootstrap.releaseExternalResources()


  }



  def retryRegistration {
    if (retryExecutorService != null && !tryingRegistration) {
      tryingRegistration = true
      NioTcpGntpClient.logger.info("Scheduling registration retry in [{}-{}]", retryTime, retryTimeUnit)
      retryExecutorService.schedule( new Runnable {
        def run {
          register
        }
      }, retryTime, retryTimeUnit)
    }
  }
}
