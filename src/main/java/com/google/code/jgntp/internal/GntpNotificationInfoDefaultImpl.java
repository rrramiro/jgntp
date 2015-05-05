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

import com.google.code.jgntp.*;
import com.google.common.base.*;

public class GntpNotificationInfoDefaultImpl implements GntpNotificationInfo {

	private final GntpApplicationInfo applicationInfo;
	private final String name;
	private final String displayName;
	private final boolean enabled;
	private final RenderedImage iconImage;
	private final URI iconUri;

	public GntpNotificationInfoDefaultImpl(GntpApplicationInfo applicationInfo, String name, String displayName, boolean enabled, RenderedImage iconImage, URI iconUri) {
		Preconditions.checkNotNull(applicationInfo, "Application info must not be null");
		Preconditions.checkNotNull(name, "Notification name must not be null");
		this.applicationInfo = applicationInfo;
		this.name = name;
		this.displayName = displayName;
		this.enabled = enabled;
		this.iconImage = iconImage;
		this.iconUri = iconUri;
	}

	@Override
	public GntpApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationInfo == null) ? 0 : applicationInfo.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		GntpNotificationInfoDefaultImpl other = (GntpNotificationInfoDefaultImpl) obj;
		if (applicationInfo == null) {
			if (other.applicationInfo != null) {
				return false;
			}
		} else if (!applicationInfo.equals(other.applicationInfo)) {
			return false;
		}
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
		return String.format("name=%s, displayName=%s, enabled=%s, iconImage=%s, iconUri=%s", name, displayName, enabled, iconImage, iconUri);
	}

}
