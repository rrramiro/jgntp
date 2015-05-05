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

import com.google.common.base.*;

public class GntpMessageHeaderPredicate implements Predicate<String> {

	private final GntpMessageHeader headerToFind;

	public GntpMessageHeaderPredicate(GntpMessageHeader headerToFind) {
		Preconditions.checkNotNull(headerToFind, "Header to find must not be null");
		this.headerToFind = headerToFind;
	}

	@Override
	public boolean apply(String input) {
		return headerToFind.toString().equals(input);
	}

}
