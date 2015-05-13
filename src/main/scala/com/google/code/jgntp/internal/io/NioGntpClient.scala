package com.google.code.jgntp.internal.io

import java.net._
import java.util.concurrent._
import org.slf4j._
import com.google.code.jgntp._
import com.google.common.base._
import com.google.common.collect._

object NioGntpClient {
  private val logger: Logger = LoggerFactory.getLogger(classOf[NioGntpClient])
}

abstract class NioGntpClient(applicationInfo: GntpApplicationInfo, growlAddress: SocketAddress, password: GntpPassword, encrypted: Boolean) extends GntpClient {
  Preconditions.checkNotNull(applicationInfo, "Application info must not be null".asInstanceOf[AnyRef])
  Preconditions.checkNotNull(growlAddress, "Address must not be null".asInstanceOf[AnyRef])
  if (encrypted) {
    Preconditions.checkNotNull(password, "Password must not be null if sending encrypted messages".asInstanceOf[AnyRef])
  }

  private final val registrationLatch: CountDownLatch = new CountDownLatch(1)
  @volatile
  private var closed: Boolean = false


  protected def doRegister

  protected def doNotify(notification: GntpNotification)

  @throws(classOf[InterruptedException])
  protected def doShutdown(timeout: Long, unit: TimeUnit)

  private[io] def getNotificationsSent: BiMap[Long, AnyRef]

  private[io] def retryRegistration

  def register {
    if (isShutdown) {
      throw new IllegalStateException("GntpClient has been shutdown")
    }
    NioGntpClient.logger.debug("Registering GNTP application [{}]", applicationInfo)
    doRegister
  }

  def isRegistered: Boolean = {
    val isRegistered: Boolean = registrationLatch.getCount == 0 && !isShutdown
    NioGntpClient.logger.debug("checking if the [{}] application is registered. Registered = {}", applicationInfo, isRegistered)
    return isRegistered
  }

  @throws(classOf[InterruptedException])
  def waitRegistration {
    registrationLatch.await
  }

  @throws(classOf[InterruptedException])
  def waitRegistration(time: Long, unit: TimeUnit): Boolean = {
    return registrationLatch.await(time, unit)
  }

  def notify(notification: GntpNotification) {
    var interrupted: Boolean = false
    var notified = false
    while (!isShutdown && !notified) {
      try {
        waitRegistration
        notifyInternal(notification)
        notified = true
        //break //todo: break is not supported
      }
      catch {
        case e: InterruptedException => {
          interrupted = true
        }
      }
    }
    if (interrupted) {
      Thread.currentThread.interrupt
    }
  }

  @throws(classOf[InterruptedException])
  def notify(notification: GntpNotification, time: Long, unit: TimeUnit): Boolean = {
    if (waitRegistration(time, unit)) {
      notifyInternal(notification)
      return true
    }
    return false
  }

  @throws(classOf[InterruptedException])
  def shutdown(timeout: Long, unit: TimeUnit) {
    closed = true
    registrationLatch.countDown
    doShutdown(timeout, unit)
  }

  def isShutdown: Boolean = {
    return closed
  }

  private[io] def setRegistered {
    registrationLatch.countDown
  }

  protected def notifyInternal(notification: GntpNotification) {
    if (!isShutdown) {
      NioGntpClient.logger.debug("Sending notification [{}]", notification)
      doNotify(notification)
    }
  }

  protected def getApplicationInfo: GntpApplicationInfo = {
    return applicationInfo
  }

  protected def getPassword: GntpPassword = {
    return password
  }

  protected def isEncrypted: Boolean = {
    return encrypted
  }

  protected def getGrowlAddress: SocketAddress = {
    return growlAddress
  }

  protected def getRegistrationLatch: CountDownLatch = {
    return registrationLatch
  }
}


/*
 *
import java.net.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.code.jgntp.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public abstract class NioGntpClient implements GntpClient {

  private static final Logger logger = LoggerFactory.getLogger(NioGntpClient.class);

  private final GntpApplicationInfo applicationInfo;
  private final GntpPassword password;
  private final boolean encrypted;

  private final SocketAddress growlAddress;
  private final CountDownLatch registrationLatch;
  private volatile boolean closed;

  public NioGntpClient(GntpApplicationInfo applicationInfo, SocketAddress growlAddress, GntpPassword password, boolean encrypted) {
    Preconditions.checkNotNull(applicationInfo, "Application info must not be null");
    Preconditions.checkNotNull(growlAddress, "Address must not be null");
    if (encrypted) {
      Preconditions.checkNotNull(password, "Password must not be null if sending encrypted messages");
    }

    this.applicationInfo = applicationInfo;
    this.password = password;
    this.encrypted = encrypted;

    this.growlAddress = growlAddress;
    this.registrationLatch = new CountDownLatch(1);
  }

  protected abstract void doRegister();

  protected abstract void doNotify(GntpNotification notification);

  protected abstract void doShutdown(long timeout, TimeUnit unit) throws InterruptedException;

  abstract BiMap<Long, Object> getNotificationsSent();

  abstract void retryRegistration();

  public void register() {
    if (isShutdown()) {
      throw new IllegalStateException("GntpClient has been shutdown");
    }
    logger.debug("Registering GNTP application [{}]", applicationInfo);
    doRegister();
  }

  @Override
  public boolean isRegistered() {
                boolean isRegistered = registrationLatch.getCount() == 0 && !isShutdown();
                logger.debug("checking if the [{}] application is registered. Registered = {}", applicationInfo,isRegistered);
    return isRegistered;
  }

@Override
  public void waitRegistration() throws InterruptedException {
    registrationLatch.await();
  }

@Override
  public boolean waitRegistration(long time, TimeUnit unit) throws InterruptedException {
    return registrationLatch.await(time, unit);
  }

@Override
  public void notify(GntpNotification notification) {
    boolean interrupted = false;
    while (!isShutdown()) {
      try {
        waitRegistration();
        notifyInternal(notification);
        break;
      } catch (InterruptedException e) {
        interrupted = true;
      }
    }
    if (interrupted) {
      Thread.currentThread().interrupt();
    }
  }

@Override
  public boolean notify(GntpNotification notification, long time, TimeUnit unit) throws InterruptedException {
    if (waitRegistration(time, unit)) {
      notifyInternal(notification);
      return true;
    }
    return false;
  }

@Override
  public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
    closed = true;
    registrationLatch.countDown();
    doShutdown(timeout, unit);
  }

@Override
  public boolean isShutdown() {
    return closed;
  }

  void setRegistered() {
    registrationLatch.countDown();
  }

  protected void notifyInternal(final GntpNotification notification) {
    if (!isShutdown()) {
      logger.debug("Sending notification [{}]", notification);
      doNotify(notification);
    }
  }

  protected GntpApplicationInfo getApplicationInfo() {
    return applicationInfo;
  }

  protected GntpPassword getPassword() {
    return password;
  }

  protected boolean isEncrypted() {
    return encrypted;
  }

  protected SocketAddress getGrowlAddress() {
    return growlAddress;
  }

  protected CountDownLatch getRegistrationLatch() {
    return registrationLatch;
  }
}

 */