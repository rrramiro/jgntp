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

import com.google.code.jgntp.*;

public class GntpErrorMessage extends GntpMessageResponse {

	private final GntpErrorStatus status;
	private final String description;

	public GntpErrorMessage(long internalNotificationId, GntpMessageType respondingType, GntpErrorStatus status, String description) {
		super(GntpMessageType.ERROR, respondingType, internalNotificationId);
		this.status = status;
		this.description = description;
	}

	public GntpErrorStatus getStatus() {
		return status;
	}

	public String getDescription() {
		return description;
	}
}
