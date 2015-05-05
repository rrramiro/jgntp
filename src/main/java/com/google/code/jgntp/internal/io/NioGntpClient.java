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
