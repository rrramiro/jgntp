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

public enum GntpErrorStatus {

	RESERVED(100),
	TIMED_OUT(200),
	NETWORK_FAILURE(201),
	INVALID_REQUEST(300),
	UNKNOWN_PROTOCOL(301),
	UNKNOWN_PROTOCOL_VERSION(302),
	REQUIRED_HEADER_MISSING(303),
	NOT_AUTHORIZED(400),
	UNKNOWN_APPLICATION(401),
	UNKNOWN_NOTIFICATION(402),
	INTERNAL_SERVER_ERROR(500),
	;

	private int code;

	private GntpErrorStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static GntpErrorStatus parse(String s) {
		return of(Integer.parseInt(s));
	}

	public static GntpErrorStatus of(int i) {
		for (GntpErrorStatus status : values()) {
			if (status.getCode() == i) {
				return status;
			}
		}
		return null;
	}
}
