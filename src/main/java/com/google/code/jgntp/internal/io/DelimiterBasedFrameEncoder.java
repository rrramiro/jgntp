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
package com.google.code.jgntp.internal.io;

import static org.jboss.netty.buffer.ChannelBuffers.*;

import java.io.*;
import java.util.concurrent.atomic.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.ChannelHandler.*;
import org.jboss.netty.handler.codec.oneone.*;
import org.slf4j.*;

import com.google.common.io.*;

@Sharable
public class DelimiterBasedFrameEncoder extends OneToOneEncoder {

	private static final String DUMP_MESSAGES_DIRECTORY_PROPERTY = "gntp.request.dump.dir";

	private static final Logger logger = LoggerFactory.getLogger(DelimiterBasedFrameEncoder.class);

	private final ChannelBuffer delimiter;
	private File dumpDir;
	private AtomicLong dumpCounter;

	public DelimiterBasedFrameEncoder(ChannelBuffer delimiter) {
		this.delimiter = copiedBuffer(delimiter);
		String dumpDirName = System.getProperty(DUMP_MESSAGES_DIRECTORY_PROPERTY);
		dumpDir = dumpDirName == null ? null : new File(dumpDirName);
		if (dumpDir != null) {
			dumpCounter = new AtomicLong();
			try {
				Files.createParentDirs(dumpDir);
			} catch (IOException e) {
				logger.warn("Could not get/create GNTP request dump directory, dumping will be disabled", e);
				dumpDir = null;
			}
		}
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}
		ChannelBuffer buffer = copiedBuffer((ChannelBuffer) msg, delimiter);

		if (dumpDir != null) {
			try {
				String fileName = "gntp-request-" + dumpCounter.getAndIncrement() + ".out";
				byte[] b = new byte[buffer.readableBytes()];
				buffer.getBytes(0, b);
				Files.write(b, new File(dumpDir, fileName));
			} catch (IOException e) {
				logger.warn("Could not save GNTP request dump", e);
			}
		}

		return buffer;
	}

}
