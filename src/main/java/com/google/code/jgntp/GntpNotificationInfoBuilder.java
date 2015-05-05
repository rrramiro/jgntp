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

import com.google.code.jgntp.internal.*;

@SuppressWarnings("hiding")
public class GntpNotificationInfoBuilder {

	private GntpApplicationInfo applicationInfo;
	private String name;
	private String displayName;
	private boolean enabled;
	private RenderedImage iconImage;
	private URI iconUri;

	public GntpNotificationInfoBuilder(GntpApplicationInfo applicationInfo, String name) {
		this.applicationInfo = applicationInfo;
		this.name = name;
		this.enabled = true;
	}

	public GntpNotificationInfoBuilder applicationInfo(GntpApplicationInfo applicationInfo) {
		this.applicationInfo = applicationInfo;
		return this;
	}

	public GntpNotificationInfoBuilder name(String name) {
		this.name = name;
		return this;
	}

	public GntpNotificationInfoBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public GntpNotificationInfoBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public GntpNotificationInfoBuilder icon(RenderedImage image) {
		iconImage = image;
		iconUri = null;
		return this;
	}

	public GntpNotificationInfoBuilder icon(URI uri) {
		iconUri = uri;
		iconImage = null;
		return this;
	}

	public GntpNotificationInfo build() {
		GntpNotificationInfo info = new GntpNotificationInfoDefaultImpl(applicationInfo, name, displayName, enabled, iconImage, iconUri);
		applicationInfo.addNotificationInfo(info);
		return info;
	}
}
