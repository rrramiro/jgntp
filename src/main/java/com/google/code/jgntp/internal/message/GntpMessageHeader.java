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

import com.google.common.base.*;
import com.google.common.collect.*;

public enum GntpMessageHeader {

	APPLICATION_NAME {
		@Override
		public String toString() {
			return "Application-Name";
		}
	},

	APPLICATION_ICON {
		@Override
		public String toString() {
			return "Application-Icon";
		}
	},

	NOTIFICATION_COUNT {
		@Override
		public String toString() {
			return "Notifications-Count";
		}
	},

	NOTIFICATION_INTERNAL_ID {
		@Override
		public String toString() {
                        // Growl 1.3.1 on OS X Lion doesn't return "Data-..." headers, but "X-..." headers.
			return "X-Data-Internal-Notification-ID";
		}
	},

	NOTIFICATION_ID {
		@Override
		public String toString() {
			return "Notification-ID";
		}
	},

	NOTIFICATION_NAME {
		@Override
		public String toString() {
			return "Notification-Name";
		}
	},

	NOTIFICATION_DISPLAY_NAME {
		@Override
		public String toString() {
			return "Notification-Display-Name";
		}
	},

	NOTIFICATION_TITLE {
		@Override
		public String toString() {
			return "Notification-Title";
		}
	},

	NOTIFICATION_ENABLED {
		@Override
		public String toString() {
			return "Notification-Enabled";
		}
	},

	NOTIFICATION_ICON {
		@Override
		public String toString() {
			return "Notification-Icon";
		}
	},

	NOTIFICATION_TEXT {
		@Override
		public String toString() {
			return "Notification-Text";
		}
	},

	NOTIFICATION_STICKY {
		@Override
		public String toString() {
			return "Notification-Sticky";
		}
	},

	NOTIFICATION_PRIORITY {
		@Override
		public String toString() {
			return "Notification-Priority";
		}
	},

	NOTIFICATION_COALESCING_ID {
		@Override
		public String toString() {
			return "Notification-Coalescing-ID";
		}
	},

	NOTIFICATION_CALLBACK_TARGET {
		@Override
		public String toString() {
			return "Notification-Callback-Target";
		}
	},

	NOTIFICATION_CALLBACK_CONTEXT {
		@Override
		public String toString() {
			return "Notification-Callback-Context";
		}
	},

	NOTIFICATION_CALLBACK_CONTEXT_TYPE {
		@Override
		public String toString() {
			return "Notification-Callback-Context-Type";
		}
	},

	NOTIFICATION_CALLBACK_RESULT {
		@Override
		public String toString() {
			return "Notification-Callback-Result";
		}
	},

	NOTIFICATION_CALLBACK_TIMESTAMP {
		@Override
		public String toString() {
			return "Notification-Callback-Timestamp";
		}
	},

	RESPONSE_ACTION {
		@Override
		public String toString() {
			return "Response-Action";
		}
	},

	ERROR_CODE {
		@Override
		public String toString() {
			return "Error-Code";
		}
	},

	ERROR_DESCRIPTION {
		@Override
		public String toString() {
			return "Error-Description";
		}
	},
	;

	public Predicate<String> getPredicate() {
		return new GntpMessageHeaderPredicate(this);
	}

	public String getValueInMap(Map<String, String> map) {
		Map<String, String> filteredMap = Maps.filterKeys(map, getPredicate());
		if (filteredMap.isEmpty()) {
			return null;
		}
		return filteredMap.get(toString());
	}

	public String getRequiredValueInMap(Map<String, String> map) {
		String value = getValueInMap(map);
		Preconditions.checkState(value != null, "Required header [%s] not found", this);
		return value;
	}
}
