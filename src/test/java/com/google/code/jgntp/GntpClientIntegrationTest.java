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

import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;

import org.junit.*;
import org.slf4j.*;

import com.google.common.io.*;

import static java.util.concurrent.TimeUnit.*;

public class GntpClientIntegrationTest {

	public static final String APPLICATION_ICON = "app-icon.png";
	public static final String RING_ICON = "ring.png";
	public static final String SMS_ICON = "sms.png";
	public static final String MMS_ICON = "mms.png";
	public static final String BATTERY_ICON = "battery100.png";
	public static final String VOICEMAIL_ICON = "voicemail.png";
	public static final String PING_ICON = APPLICATION_ICON;

	private static Logger logger = LoggerFactory.getLogger(GntpClientIntegrationTest.class);

	@Test
	@SuppressWarnings("unused")
	public void test() throws Exception {
                logger.info("Running GntpClientIntegrationTest");
		GntpApplicationInfo info = Gntp.appInfo("Test").icon(getImage(APPLICATION_ICON)).build();
		GntpNotificationInfo notif1 = Gntp.notificationInfo(info, "Notify 1").icon(getImage(RING_ICON)).build();
		GntpNotificationInfo notif2 = Gntp.notificationInfo(info, "Notify 2").icon(getImage(SMS_ICON)).build();
		GntpNotificationInfo notif3 = Gntp.notificationInfo(info, "Notify 3").icon(getImage(MMS_ICON)).build();
		GntpNotificationInfo notif4 = Gntp.notificationInfo(info, "Notify 4").icon(getImage(BATTERY_ICON)).build();
		GntpNotificationInfo notif5 = Gntp.notificationInfo(info, "Notify 5").icon(getImage(VOICEMAIL_ICON)).build();
		GntpNotificationInfo notif6 = Gntp.notificationInfo(info, "Notify 6").icon(getImage(PING_ICON)).build();

		GntpClient client = Gntp.client(info).forHost("localhost").forGrowl_1_3().listener(new GntpListener() {
			@Override
			public void onRegistrationSuccess() {
				logger.info("Registered");
			}

			@Override
			public void onNotificationSuccess(GntpNotification notification) {
				logger.info("Notification success: " + notification);
			}

			@Override
			public void onClickCallback(GntpNotification notification) {
				logger.info("Click callback: " + notification.getContext());
			}

			@Override
			public void onCloseCallback(GntpNotification notification) {
				logger.info("Close callback: " + notification.getContext());
			}

			@Override
			public void onTimeoutCallback(GntpNotification notification) {
				logger.info("Timeout callback: " + notification.getContext());
			}

			@Override
			public void onRegistrationError(GntpErrorStatus status, String description) {
				logger.info("Registration Error: " + status + " - desc: " + description);
			}

			@Override
			public void onNotificationError(GntpNotification notification, GntpErrorStatus status, String description) {
				logger.info("Notification Error: " + status + " - desc: " + description);
			}

			@Override
			public void onCommunicationError(Throwable t) {
				logger.error("Communication error", t);
			}
		}).build();

		client.register();
		client.notify(Gntp.notification(notif1, "Title")
						.text("Message")
						.withCallback()
						.context(12345)
						.header(Gntp.APP_SPECIFIC_HEADER_PREFIX + "Filename", "file.txt")
						.build(), 5, SECONDS);

		client.notify(Gntp.notification(notif2, "Title 2")
						.text("Message 2")
						.icon(getImage(APPLICATION_ICON))
						.callbackTarget(URI.create("http://slashdot.org/"))
						.header(Gntp.CUSTOM_HEADER_PREFIX + "Payload", getClass().getResourceAsStream("sms.png"))
						.build(), 5, SECONDS);

		SECONDS.sleep(5);
		client.shutdown(5, SECONDS);
	}
	
	protected RenderedImage getImage(String name) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		try {
			return ImageIO.read(is);
		} finally {
			Closeables.closeQuietly(is);
		}
	}
}
