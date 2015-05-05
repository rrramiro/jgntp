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
package com.google.code.jgntp.internal.message.write;

import java.io.*;

import com.google.code.jgntp.internal.message.GntpMessage.*;

public class ClearTextGntpMessageWriter extends AbstractGntpMessageWriter {

	public static final String NONE_ENCRYPTION_ALGORITHM = "NONE";

	@Override
	protected void writeEncryptionSpec() throws IOException {
		writer.append(NONE_ENCRYPTION_ALGORITHM);
	}

	@Override
	protected byte[] getDataForBinarySection(BinarySection binarySection) {
		return binarySection.getData();
	}

}
