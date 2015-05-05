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

import org.junit.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.util.*;

import static org.junit.Assert.*;

public class GntpPasswordDefaultImplTest {

	@Test
	public void testKeyGeneration() throws Exception {
		GntpPassword password = new GntpPasswordDefaultImpl("test") {
			@Override
			protected byte[] getSalt(String randomSaltAlgorithm) {
				return Hex.fromHexadecimal("26EFBFBD0EEFBFBD206359");
			}
		};
		byte[] expectedKey = Hex.fromHexadecimal("FC164AC0B12DFAF6D796C61BE16542A1108FA442997FDC447D569C03820605113D23C4AE42387FE6060EE1ED4F6872BC30F67A63C2CE4F02695B061631230815");
		assertArrayEquals(password.getKeyHash(), expectedKey);
	}
}
