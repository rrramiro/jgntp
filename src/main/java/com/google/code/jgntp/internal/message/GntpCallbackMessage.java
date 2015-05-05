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

import java.util.*;

public class GntpCallbackMessage extends GntpMessageResponse {

	private final String notificationId;
	private final GntpCallbackResult callbackResult;
	private final String context;
	private final String contextType;
	private final Date timestamp;

	public GntpCallbackMessage(long internalNotificationId, String notificationId, GntpCallbackResult callbackResult, String context, String contextType, Date timestamp) {
		super(GntpMessageType.CALLBACK, GntpMessageType.NOTIFY, internalNotificationId);
		this.notificationId = notificationId;
		this.callbackResult = callbackResult;
		this.context = context;
		this.contextType = contextType;
		this.timestamp = timestamp;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public GntpCallbackResult getCallbackResult() {
		return callbackResult;
	}

	public String getContext() {
		return context;
	}

	public String getContextType() {
		return contextType;
	}

	public Date getTimestamp() {
		return timestamp;
	}

}
