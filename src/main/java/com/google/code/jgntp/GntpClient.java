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

import java.util.concurrent.*;

public interface GntpClient {
	/**
	 * Send registration request asynchronously.
	 */
	void register();

	/**
	 * @return True if this client is registered.
	 */
	boolean isRegistered();

	/**
	 * Wait until this client is registered or shutted down or the current thread is
	 * interrupted.
	 * Prefer a timed wait if you can.
	 * 
	 * @throws InterruptedException If the current thread is interrupted.
	 */
	void waitRegistration() throws InterruptedException;

	/**
	 * Wait until this client is registered within the given waiting time.
	 * 
	 * @param time The maximum time to wait.
	 * @param unit The time unit of the {@code time} argument.
	 * @throws InterruptedException If the current thread is interrupted.
	 * @return True if this client registered successfully before the waiting
	 *         time elapsed, false otherwise.
	 */
	boolean waitRegistration(long time, TimeUnit unit) throws InterruptedException;

	/**
	 * Send the given notification waiting uninterruptbly if this client is not
	 * registered yet. Wait until this client is registered or is shutdown.
	 * 
	 * @param notification Notification to send.
	 */
	void notify(GntpNotification notification);

	/**
	 * Send the given notification waiting at most the given time if this client
	 * is not registered yet.
	 * 
	 * @param notification Notification to send.
	 * @param time The maximum time to wait.
	 * @param unit The time unit of the {@code time} argument.
	 * @return True if this client sent the notification successfully before the waiting
	 *         time elapsed, false otherwise.
	 * @throws InterruptedException If the current thread is interrupted.
	 */
	boolean notify(GntpNotification notification, long time, TimeUnit unit) throws InterruptedException;

	/**
	 * Shutdown this client waiting at most the given time.
	 * 
	 * @param time The maximum time to wait for shutdown.
	 * @param unit The time unit of the {@code time} argument.
	 * @throws InterruptedException If the current thread is interrupted.
	 */
	void shutdown(long time, TimeUnit unit) throws InterruptedException;
	
	/**
	 * @return True if this client is shut down.
	 */
	boolean isShutdown();
}
