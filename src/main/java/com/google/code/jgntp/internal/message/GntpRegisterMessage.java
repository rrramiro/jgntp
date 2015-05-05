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
package com.google.code.jgntp.internal.message;

import java.io.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.write.*;

public class GntpRegisterMessage extends GntpMessage {

	private final GntpApplicationInfo info;

	public GntpRegisterMessage(GntpApplicationInfo applicationInfo, GntpPassword password, boolean encrypt) {
		super(GntpMessageType.REGISTER, password, encrypt);
		info = applicationInfo;
	}

	@Override
	public void append(OutputStream output) throws IOException {
		GntpMessageWriter writer = getWriter(output);
		appendStatusLine(writer);
		appendSeparator(writer);
		
		writer.startHeaders();

		appendHeader(GntpMessageHeader.APPLICATION_NAME, info.getName(), writer);
		appendSeparator(writer);

		if (appendIcon(GntpMessageHeader.APPLICATION_ICON, info.getIconImage(), info.getIconUri(), writer)) {
			appendSeparator(writer);
		}

		appendHeader(GntpMessageHeader.NOTIFICATION_COUNT, info.getNotificationInfos().size(), writer);
		appendSeparator(writer);

		appendSeparator(writer);

		for (GntpNotificationInfo notificationInfo : info.getNotificationInfos()) {
			appendHeader(GntpMessageHeader.NOTIFICATION_NAME, notificationInfo.getName(), writer);
			appendSeparator(writer);

			if (notificationInfo.getDisplayName() != null) {
				appendHeader(GntpMessageHeader.NOTIFICATION_DISPLAY_NAME, notificationInfo.getDisplayName(), writer);
				appendSeparator(writer);
			}

			if (appendIcon(GntpMessageHeader.NOTIFICATION_ICON, notificationInfo.getIconImage(), notificationInfo.getIconUri(), writer)) {
				appendSeparator(writer);
			}

			appendHeader(GntpMessageHeader.NOTIFICATION_ENABLED, notificationInfo.isEnabled(), writer);
			appendSeparator(writer);

			appendSeparator(writer);
		}
		writer.finishHeaders();

		appendBinarySections(writer);
		clearBinarySections();
	}
}
