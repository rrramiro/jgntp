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
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.socket.*;
import org.jboss.netty.channel.socket.oio.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class NioUdpGntpClient extends NioGntpClient {

	// UDP client does not keep track of notifications sent
	private final static BiMap<Long, Object> notificationsSent = HashBiMap.create();

	private final ConnectionlessBootstrap bootstrap;
	private final DatagramChannel datagramChannel;

	public NioUdpGntpClient(GntpApplicationInfo applicationInfo, SocketAddress growlAddress, Executor executor, GntpPassword password, boolean encrypted) {
		super(applicationInfo, growlAddress, password, encrypted);
		Preconditions.checkNotNull(executor, "Executor must not be null");

		bootstrap = new ConnectionlessBootstrap(new OioDatagramChannelFactory(executor));
		bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, null)));
		bootstrap.setOption("broadcast", "false");

		datagramChannel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));
	}

	@Override
	protected void doRegister() {
		GntpMessage message = new GntpRegisterMessage(getApplicationInfo(), getPassword(), isEncrypted());
		datagramChannel.write(message, getGrowlAddress());
		getRegistrationLatch().countDown();
	}

	@Override
	protected void doNotify(GntpNotification notification) {
		GntpNotifyMessage message = new GntpNotifyMessage(notification, -1, getPassword(), isEncrypted());
		datagramChannel.write(message, getGrowlAddress());
	}

	@Override
	protected void doShutdown(long timeout, TimeUnit unit) throws InterruptedException {
		datagramChannel.close().await(timeout, unit);
		bootstrap.releaseExternalResources();
	}

	@Override
	BiMap<Long, Object> getNotificationsSent() {
		return notificationsSent;
	}

	@Override
	void retryRegistration() {
		// Do nothing
	}
}
