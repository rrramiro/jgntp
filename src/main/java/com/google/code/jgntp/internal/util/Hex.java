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
package com.google.code.jgntp.internal.util;

import com.google.common.base.*;

public class Hex {

	private static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private Hex() {
	}

	public static String toHexadecimal(byte[] array) {
		if (array == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer(array.length * 2);
		for (int i = 0; i < array.length; i++) {
			int curByte = array[i] & 0xff;
			buffer.append(HEX_DIGITS[(curByte >> 4)]);
			buffer.append(HEX_DIGITS[curByte & 0xf]);
		}
		return buffer.toString();
	}

	public static byte[] fromHexadecimal(String s) {
		if (s == null) {
			return null;
		}
		Preconditions.checkArgument(s.length() % 2 == 0, "Invalid hex string [%s]", s);
		byte[] result = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i = i + 2) {
			int first = Character.digit(s.charAt(i), 16);
			int second = Character.digit(s.charAt(i + 1), 16);
			result[i / 2] = (byte) (0x0 + ((first & 0xff) << 4) + (second & 0xff));
		}
		return result;
	}

}
