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

public enum GntpCallbackResult {

	CLICK("CLICKED", "CLICK"), CLOSE("CLOSED", "CLOSE"), TIMEOUT("TIMEDOUT", "TIMEOUT");

	private String[] names;

	private GntpCallbackResult(String... names) {
		this.names = names;
		Arrays.sort(names);
	}

	public String[] getNames() {
		return names;
	}

	public static GntpCallbackResult parse(String s) {
		for (GntpCallbackResult result : values()) {
			if (Arrays.binarySearch(result.names, s) >= 0) {
				return result;
			}
		}
		return null;
	}
}
