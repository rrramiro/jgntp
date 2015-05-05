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
package com.google.code.jgntp.internal;

import java.awt.image.*;
import java.net.*;
import java.util.*;

import com.google.code.jgntp.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class GntpApplicationInfoDefaultImpl implements GntpApplicationInfo {

	private final String name;
	private final RenderedImage iconImage;
	private final URI iconUri;
	private final List<GntpNotificationInfo> notificationInfos;

	public GntpApplicationInfoDefaultImpl(String name, RenderedImage iconImage, URI iconUri) {
		Preconditions.checkNotNull(name, "Application name must not be null");
		this.name = name;
		this.iconImage = iconImage;
		this.iconUri = iconUri;
		notificationInfos = Lists.newArrayList();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RenderedImage getIconImage() {
		return iconImage;
	}

	@Override
	public URI getIconUri() {
		return iconUri;
	}

	@Override
	public List<GntpNotificationInfo> getNotificationInfos() {
		return notificationInfos;
	}

	@Override
	public void addNotificationInfo(GntpNotificationInfo notificationInfo) {
		notificationInfos.add(notificationInfo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GntpApplicationInfoDefaultImpl other = (GntpApplicationInfoDefaultImpl) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
