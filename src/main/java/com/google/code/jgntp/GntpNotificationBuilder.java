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
import java.net.*;
import java.util.*;

import com.google.code.jgntp.GntpNotification.*;
import com.google.code.jgntp.internal.*;
import com.google.common.base.*;
import com.google.common.collect.*;

@SuppressWarnings("hiding")
public class GntpNotificationBuilder {

	private String applicationName;
	private String name;
	private String id;
	private String title;
	private String text;
	private Boolean sticky;
	private Priority priority;
	private RenderedImage iconImage;
	private URI iconUri;
	private String coalescingId;
	private URI callbackTarget;
	private boolean callbackRequested;
	private Object context;
	private Map<String, Object> customHeaders;

	public GntpNotificationBuilder(GntpNotificationInfo info, String title) {
		Preconditions.checkNotNull(info, "Notification info must not be null");
		applicationName = info.getApplicationInfo().getName();
		name = info.getName();
		this.title = title;
		this.customHeaders = Maps.newHashMap();
	}

	public GntpNotificationBuilder info(GntpNotificationInfo info) {
		Preconditions.checkNotNull(info, "Notification info must not be null");
		applicationName = info.getApplicationInfo().getName();
		name = info.getName();
		return this;
	}

	public GntpNotificationBuilder id(String id) {
		this.id = id;
		return this;
	}

	public GntpNotificationBuilder title(String title) {
		Preconditions.checkNotNull(title, "Notification title must not be null");
		this.title = title;
		return this;
	}

	public GntpNotificationBuilder text(String text) {
		this.text = text;
		return this;
	}

	public GntpNotificationBuilder sticky(Boolean sticky) {
		this.sticky = sticky;
		return this;
	}

	public GntpNotificationBuilder priority(Priority priority) {
		this.priority = priority;
		return this;
	}

	public GntpNotificationBuilder icon(RenderedImage image) {
		iconImage = image;
		iconUri = null;
		return this;
	}

	public GntpNotificationBuilder icon(URI uri) {
		iconUri = uri;
		iconImage = null;
		return this;
	}

	public GntpNotificationBuilder coalescingId(String coalescingId) {
		this.coalescingId = coalescingId;
		return this;
	}

	public GntpNotificationBuilder callbackTarget(URI callbackTarget) {
		this.callbackTarget = callbackTarget;
		callbackRequested = false;
		return this;
	}

	public GntpNotificationBuilder withCallback() {
		callbackRequested = true;
		callbackTarget = null;
		return this;
	}

	public GntpNotificationBuilder withoutCallback() {
		callbackRequested = false;
		return this;
	}

	public GntpNotificationBuilder context(Object context) {
		this.context = context;
		return this;
	}

	public GntpNotificationBuilder header(String name, Object value) {
		customHeaders.put(name, value);
		return this;
	}

	public GntpNotification build() {
		return new GntpNotificationDefaultImpl(applicationName, name, id, title, text, sticky, priority, iconImage, iconUri, coalescingId, callbackTarget, callbackRequested, context, customHeaders);
	}
}
