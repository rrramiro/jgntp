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
package com.google.code.jgntp.internal.io;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.nio.*;
import org.slf4j.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class NioTcpGntpClient extends NioGntpClient {

	private static final Logger logger = LoggerFactory.getLogger(NioTcpGntpClient.class);

	private final long retryTime;
	private final TimeUnit retryTimeUnit;
	private final int notificationRetryCount;

	private final ClientBootstrap bootstrap;
	private final ChannelGroup channelGroup;
	private final ScheduledExecutorService retryExecutorService;
	private volatile boolean tryingRegistration;

	private final AtomicLong notificationIdGenerator;
	private final BiMap<Long, Object> notificationsSent;

	private final Map<GntpNotification, Integer> notificationRetries;

	public NioTcpGntpClient(GntpApplicationInfo applicationInfo, SocketAddress growlAddress, Executor executor, GntpListener listener, GntpPassword password, boolean encrypted, long retryTime,
			TimeUnit retryTimeUnit, int notificationRetryCount) {
		super(applicationInfo, growlAddress, password, encrypted);
		Preconditions.checkNotNull(executor, "Executor must not be null");
		if (retryTime > 0) {
			Preconditions.checkNotNull(retryTimeUnit, "Retry time unit must not be null");
		}
		Preconditions.checkArgument(notificationRetryCount >= 0, "Notification retries must be equal or greater than zero");

		this.retryTime = retryTime;
		this.retryTimeUnit = retryTimeUnit;
		this.notificationRetryCount = notificationRetryCount;

		if (retryTime > 0) {
			retryExecutorService = Executors.newSingleThreadScheduledExecutor();
		} else {
			retryExecutorService = null;
		}

		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor));
		bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, listener)));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("remoteAddress", growlAddress);
		bootstrap.setOption("soTimeout", 60 * 1000);
		bootstrap.setOption("receiveBufferSizePredictor", new AdaptiveReceiveBufferSizePredictor());
		channelGroup = new DefaultChannelGroup("jgntp");

		notificationIdGenerator = new AtomicLong();
		// no need to sync on this map because notificationIdGenerator guarantees ID uniqueness
		// and there is only one way for alterations to this map occur for any notification
		// see GntpChannelHandler for details
		notificationsSent = HashBiMap.create();

		notificationRetries = Maps.newConcurrentMap();
	}

	@Override
	protected void doRegister() {
		bootstrap.connect().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				tryingRegistration = false;
				if (future.isSuccess()) {
					channelGroup.add(future.getChannel());
					GntpMessage message = new GntpRegisterMessage(getApplicationInfo(), getPassword(), isEncrypted());
					future.getChannel().write(message);
				}
			}
		});
	}

	@Override
	protected void doNotify(final GntpNotification notification) {
		bootstrap.connect().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					channelGroup.add(future.getChannel());

					long notificationId = notificationIdGenerator.getAndIncrement();
					notificationsSent.put(notificationId, notification);

					GntpMessage message = new GntpNotifyMessage(notification, notificationId, getPassword(), isEncrypted());
					future.getChannel().write(message);
				} else {
					if (retryExecutorService != null) {
						Integer count = notificationRetries.get(notification);
						if (count == null) {
							count = 1;
						}
						if (count <= notificationRetryCount) {
							logger.debug("Failed to send notification [{}], retry [{}/{}] in [{}-{}]", new Object[] { notification, count, notificationRetryCount, retryTime, retryTimeUnit });
							notificationRetries.put(notification, ++count);
							retryExecutorService.schedule(new Runnable() {
								public void run() {
									NioTcpGntpClient.this.notify(notification);
								}
							}, retryTime, retryTimeUnit);
						} else {
							logger.debug("Failed to send notification [{}], giving up", notification);
							notificationRetries.remove(notification);
						}
					}
					notificationsSent.inverse().remove(notification);
				}
			}
		});
	}
	
	@Override
	protected void doShutdown(long timeout, TimeUnit unit) throws InterruptedException {
		if (retryExecutorService != null) {
			retryExecutorService.shutdownNow();
			retryExecutorService.awaitTermination(timeout, unit);
		}
		channelGroup.close().await(timeout, unit);
		bootstrap.releaseExternalResources();
	}
	
	BiMap<Long, Object> getNotificationsSent() {
		return notificationsSent;
	}

	void retryRegistration() {
		if (retryExecutorService != null && !tryingRegistration) {
			tryingRegistration = true;
			logger.info("Scheduling registration retry in [{}-{}]", retryTime, retryTimeUnit);
			retryExecutorService.schedule(new Runnable() {
				@Override
				public void run() {
					register();
				}
			}, retryTime, retryTimeUnit);
		}
	}

}
