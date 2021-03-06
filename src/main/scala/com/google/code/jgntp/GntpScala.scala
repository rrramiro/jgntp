/*
 * Copyright (C) 2010 Leandro Aparecido <lehphyro@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.jgntp

import java.net._
import java.util.concurrent._

import com.google.code.jgntp.internal.io.NioTcpGntpClient
import com.google.code.jgntp.internal.io._
import org.slf4j.{Logger, LoggerFactory}

object GntpScala {
  val CUSTOM_HEADER_PREFIX: String = "X-"
  val APP_SPECIFIC_HEADER_PREFIX: String = "Data-"
  val WINDOWS_TCP_PORT: Int = 23053
  val MAC_TCP_PORT: Int = 23052
  val UDP_PORT: Int = 9887
  val DEFAULT_RETRY_TIME: Long = 3
  val DEFAULT_RETRY_TIME_UNIT: TimeUnit = TimeUnit.SECONDS
  val DEFAULT_NOTIFICATION_RETRIES: Int = 3
  private val logger: Logger = LoggerFactory.getLogger(getClass)


  private def getTcpPort: Int = {
    val osName: String = System.getProperty("os.name")
    if (osName != null && osName.toLowerCase.contains("mac")) {
      GntpScala.logger.debug("using mac port number: " + GntpScala.MAC_TCP_PORT)
      GntpScala.MAC_TCP_PORT
    } else {
      GntpScala.logger.debug("using the windows port for growl")
      GntpScala.WINDOWS_TCP_PORT
    }
  }


  private def getInetAddress(name: String): InetAddress = {
    if (name == null) {
      try {
        return InetAddress.getLocalHost
      }
      catch {
        case e: UnknownHostException => {
          try {
            return InetAddress.getByName(name)
          }
          catch {
            case uhe: UnknownHostException => {
              throw new IllegalStateException("Could not find localhost", uhe)
            }
          }
        }
      }
    }
    try {
      InetAddress.getByName(name)
    }
    catch {
      case e: UnknownHostException => {
        throw new IllegalStateException("Could not find inet address: " + name, e)
      }
    }
  }

  def apply(applicationInfo: GntpApplicationInfo,
            growlHost: String,
            growlPort: Int = GntpScala.getTcpPort, //UDP
            tcp: Boolean = true,
            executor: Executor = Executors.newCachedThreadPool,
            listener: GntpListener = null,
            password: GntpPassword = null,
            retryTime: Long = GntpScala.DEFAULT_RETRY_TIME, //0
            retryTimeUnit: TimeUnit = GntpScala.DEFAULT_RETRY_TIME_UNIT,
            notificationRetryCount: Int = GntpScala.DEFAULT_NOTIFICATION_RETRIES //0
             ): GntpClient = {
    assert(null != applicationInfo, "Application info must not be null")
    assert(null != growlHost, "Growl host must not be null")
    assert(growlPort > 0, "Port must not be negative")
    assert(retryTime > 0, "Retry time must be greater than zero")
    assert(null != retryTimeUnit, "Retry time unit must not be null")
    //val growlAddress: SocketAddress = new InetSocketAddress(GntpScala.getInetAddress(growlHost), growlPort)
//    if (!tcp && listener != null) {
//      throw new IllegalArgumentException("Cannot set listener on a non-TCP client")
//    }
//    if (tcp) {
      new NioTcpGntpClient(applicationInfo, GntpScala.getInetAddress(growlHost), growlPort, executor, listener, password, retryTime, retryTimeUnit, notificationRetryCount)
//    }
//    else {
//      new NioUdpGntpClient(applicationInfo, growlAddress, executor, password)
//    }
  }
}

