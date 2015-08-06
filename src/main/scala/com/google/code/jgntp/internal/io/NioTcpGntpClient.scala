package com.google.code.jgntp.internal.io

import java.net._
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit, Executor}
import scala.collection.concurrent.{TrieMap, Map}

import com.google.code.jgntp._
import com.google.code.jgntp.internal.message._
import org.jboss.netty.bootstrap._
import org.jboss.netty.channel._
import org.jboss.netty.channel.group._
import org.jboss.netty.channel.socket.nio._
import org.slf4j._

import scala.collection.mutable

class NioTcpGntpClient(applicationInfo: GntpApplicationInfo,
                       growlAddress: SocketAddress,
                       executor: Executor,
                       listener: GntpListener,
                       password: GntpPassword,
                       retryTime: Long = 0L,
                       retryTimeUnit: TimeUnit,
                       notificationRetryCount: Int = 0) extends NioGntpClient(applicationInfo, growlAddress, password) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[NioTcpGntpClient])
  assert(executor != null, "Executor must not be null")
  if (retryTime > 0) {
    assert(retryTimeUnit != null, "Retry time unit must not be null")
  }
  assert(notificationRetryCount >= 0, "Notification retries must be equal or greater than zero")

  private final val bootstrap: ClientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor))
  bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, Some(listener))))
  bootstrap.setOption("tcpNoDelay", true)
  bootstrap.setOption("remoteAddress", growlAddress)
  bootstrap.setOption("soTimeout", 60 * 1000)
  bootstrap.setOption("receiveBufferSizePredictor", new AdaptiveReceiveBufferSizePredictor)
  private final val channelGroup: ChannelGroup = new DefaultChannelGroup("jgntp")
  private final val retryExecutorService: ScheduledExecutorService = if (retryTime > 0) Executors.newSingleThreadScheduledExecutor else null
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
          channelGroup.add(future.getChannel)
          val message: GntpMessage = new GntpRegisterMessage(applicationInfo, password)
          future.getChannel.write(message)
        }
      }
    })
  }

  protected def doNotify(notification: GntpNotification) {
    bootstrap.connect.addListener(new ChannelFutureListener {
      @throws(classOf[Exception])
      def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          channelGroup.add(future.getChannel)
          val notificationId: Long = notificationIdGenerator.getAndIncrement
          notificationsSent.put(notificationId, notification)
          val message: GntpMessage = new GntpNotifyMessage(notification, notificationId, password)
          future.getChannel.write(message)
        }
        else {
          if (retryExecutorService != null) {
            var count: Integer = notificationRetries(notification)
            if (count == null) {
              count = 1
            }
            if (count <= notificationRetryCount) {
              logger.debug("Failed to send notification [{}], retry [{}/{}] in [{}-{}]", Array(notification, count, notificationRetryCount, retryTime, retryTimeUnit))
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
              logger.debug("Failed to send notification [{}], giving up", notification)
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
    bootstrap.releaseExternalResources()
  }



  def retryRegistration {
    if (retryExecutorService != null && !tryingRegistration) {
      tryingRegistration = true
      logger.info("Scheduling registration retry in [{}-{}]", retryTime, retryTimeUnit)
      retryExecutorService.schedule( new Runnable {
        def run {
          register
        }
      }, retryTime, retryTimeUnit)
    }
  }
}
