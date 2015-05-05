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

public interface GntpNotification {

	String getApplicationName();

	String getName();

	String getId();

	String getTitle();

	String getText();

	Boolean isSticky();

	Priority getPriority();

	RenderedImage getIconImage();

	URI getIconUri();

	String getCoalescingId();

	URI getCallbackTarget();

	boolean isCallbackRequested();

	Object getContext();

	Map<String, Object> getCustomHeaders();

	enum Priority {
		LOWEST(-2), LOW(-1), NORMAL(0), HIGH(1), HIGHEST(2);

		private int code;

		private Priority(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}
}
