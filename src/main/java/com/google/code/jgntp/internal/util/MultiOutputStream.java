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

import java.io.*;
import java.util.*;

/**
 * Sends written bytes to all specified output streams.
 * Exceptions are propagated as soon as they are generated,
 * it means if an exception is thrown there is no guarantee
 * of what OutputStreams have received the last bytes written.
 */
public class MultiOutputStream extends OutputStream {

	private final OutputStream[] outputStreams;

	public MultiOutputStream(OutputStream... outputStreams) {
		this.outputStreams = Arrays.copyOf(outputStreams, outputStreams.length);
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream os : outputStreams) {
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		for (OutputStream os : outputStreams) {
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (OutputStream os : outputStreams) {
			os.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		for (OutputStream os : outputStreams) {
			os.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for (OutputStream os : outputStreams) {
			os.close();
		}
	}

	public OutputStream[] getOutputStreams() {
		return Arrays.copyOf(outputStreams, outputStreams.length);
	}
}
