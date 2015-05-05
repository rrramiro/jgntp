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
package com.google.code.jgntp;

import java.net.*;
import java.util.concurrent.*;

import com.google.code.jgntp.internal.*;
import com.google.code.jgntp.internal.io.*;
import com.google.common.base.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("hiding")
public class Gntp {

	public static final String CUSTOM_HEADER_PREFIX = "X-";
	public static final String APP_SPECIFIC_HEADER_PREFIX = "Data-";

	public static final int WINDOWS_TCP_PORT = 23053;
	public static final int MAC_TCP_PORT = 23052;
	public static final int UDP_PORT = 9887;

	public static final long DEFAULT_RETRY_TIME = 3;
	public static final TimeUnit DEFAULT_RETRY_TIME_UNIT = TimeUnit.SECONDS;
	public static final int DEFAULT_NOTIFICATION_RETRIES = 3;

	private GntpApplicationInfo applicationInfo;
	private SocketAddress growlAddress;
	private boolean tcp;
	private String growlHost;
	private int growlPort;
	private Executor executor;
	private GntpListener listener;
	private GntpPassword password;
	private boolean encrypted;
	private long retryTime;
	private TimeUnit retryTimeUnit;
	private int notificationRetryCount;
        private boolean isCustomPort = false;

        private static Logger logger = LoggerFactory.getLogger(Gntp.class);
	private Gntp(GntpApplicationInfo applicationInfo) {
		Preconditions.checkNotNull(applicationInfo, "Application info must not be null");
		this.applicationInfo = applicationInfo;
		tcp = true;
		growlPort = getTcpPort();
		retryTime = DEFAULT_RETRY_TIME;
		retryTimeUnit = DEFAULT_RETRY_TIME_UNIT;
		notificationRetryCount = DEFAULT_NOTIFICATION_RETRIES;
	}

	public static GntpApplicationInfoBuilder appInfo(String name) {
		return new GntpApplicationInfoBuilder(name);
	}

	public static GntpNotificationInfoBuilder notificationInfo(GntpApplicationInfo applicationInfo, String name) {
		return new GntpNotificationInfoBuilder(applicationInfo, name);
	}

	public static GntpNotificationBuilder notification(GntpNotificationInfo info, String title) {
		return new GntpNotificationBuilder(info, title);
	}

	public static GntpPassword password(String textPassword) {
		return new GntpPasswordDefaultImpl(textPassword);
	}

	public static Gntp client(GntpApplicationInfo applicationInfo) {
		return new Gntp(applicationInfo);
	}
        
        public Gntp forGrowl_1_3() {
                if(!isCustomPort){
                        growlPort = WINDOWS_TCP_PORT;
                } else {
                    logger.debug("A custom port was used. Can't switch to windows port in case this is running on a mac. This might fail.");
                }
		return this;
	}

	public Gntp forAddress(SocketAddress address) {
		growlAddress = address;
		return this;
	}

	public Gntp forHost(String host) {
		Preconditions.checkNotNull(host, "Growl host must not be null");
		growlHost = host;
		return this;
	}

	public Gntp onPort(int port) {
                isCustomPort = true;
		Preconditions.checkArgument(port > 0, "Port must not be negative");
		growlPort = port;
		return this;
	}

	public Gntp withTcp() {
		this.tcp = true;
		if (growlPort == UDP_PORT) {
			this.growlPort = getTcpPort();
		}
		return this;
	}

	public Gntp withUdp() {
		throw new UnsupportedOperationException("The Growl UDP protocol is not supported");
	}

	public Gntp executor(Executor executor) {
		this.executor = executor;
		return this;
	}

	public Gntp listener(GntpListener listener) {
		this.listener = listener;
		return this;
	}

	public Gntp withPassword(String textPassword) {
		this.password = password(textPassword);
		return this;
	}

	public Gntp withPassword(GntpPassword password) {
		this.password = password;
		return this;
	}

	public Gntp secure(String password) {
		withPassword(password);
		this.encrypted = true;
		return this;
	}

	public Gntp secure(GntpPassword password) {
		withPassword(password);
		this.encrypted = true;
		return this;
	}

	public Gntp insecure() {
		this.encrypted = false;
		return this;
	}

	public Gntp retryingAtFixedRate(long time, TimeUnit unit) {
		Preconditions.checkArgument(time > 0, "Retry time must be greater than zero");
		Preconditions.checkArgument(unit != null, "Retry time unit must not be null");
		retryTime = time;
		retryTimeUnit = unit;
		return this;
	}

	public Gntp retryingNotificationsAtMost(int notificationRetryCount) {
		this.notificationRetryCount = notificationRetryCount;
		return this;
	}

	public Gntp withoutRetry() {
		retryTime = 0;
		retryTimeUnit = DEFAULT_RETRY_TIME_UNIT;
		notificationRetryCount = 0;
		return this;
	}

	public GntpClient build() {
		if (growlAddress == null) {
			growlAddress = new InetSocketAddress(getInetAddress(growlHost), growlPort);
		}
		if (!tcp && listener != null) {
			throw new IllegalArgumentException("Cannot set listener on a non-TCP client");
		}
		Executor executorToUse = executor == null ? Executors.newCachedThreadPool() : executor;
		if (tcp) {
			return new NioTcpGntpClient(applicationInfo, growlAddress, executorToUse, listener, password, encrypted, retryTime, retryTimeUnit, notificationRetryCount);
		} else {
			return new NioUdpGntpClient(applicationInfo, growlAddress, executorToUse, password, encrypted);
		}
	}

	private int getTcpPort() {
		String osName = System.getProperty("os.name");
		if (osName != null) {
			osName = osName.toLowerCase();
                        if (osName.contains("mac")) {
                                logger.debug("using mac port number: "+MAC_TCP_PORT);
				return MAC_TCP_PORT;
			} else {
                                logger.debug("using the windows port for growl");
                        }
		}
		return WINDOWS_TCP_PORT;
	}
	
	private InetAddress getInetAddress(String name) {
		if (name == null) {
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				try {
					return InetAddress.getByName(name);
				} catch (UnknownHostException uhe) {
					throw new IllegalStateException("Could not find localhost", uhe);
				}
			}
		}
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			throw new IllegalStateException("Could not find inet address: " + name, e);
		}
	}
}
