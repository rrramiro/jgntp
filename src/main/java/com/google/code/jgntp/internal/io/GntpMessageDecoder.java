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

import java.io.*;
import java.util.concurrent.atomic.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.ChannelHandler.*;
import org.jboss.netty.handler.codec.oneone.*;
import org.slf4j.*;

import com.google.code.jgntp.internal.message.*;
import com.google.code.jgntp.internal.message.read.*;
import com.google.common.io.*;

@Sharable
public class GntpMessageDecoder extends OneToOneDecoder {

	public static final String LOGGER_NAME = "jgntp.message";

	private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

	private static final String DUMP_MESSAGES_DIRECTORY_PROPERTY = "gntp.response.dump.dir";

	private final GntpMessageResponseParser parser;
	private File dumpDir;
	private AtomicLong dumpCounter;

	public GntpMessageDecoder() {
		parser = new GntpMessageResponseParser();
		String dumpDirName = System.getProperty(DUMP_MESSAGES_DIRECTORY_PROPERTY);
		dumpDir = dumpDirName == null ? null : new File(dumpDirName);
		if (dumpDir != null) {
			dumpCounter = new AtomicLong();
			try {
				Files.createParentDirs(dumpDir);
			} catch (IOException e) {
				logger.warn("Could not get/create GNTP response dump directory, dumping will be disabled", e);
				dumpDir = null;
			}
		}
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer) msg;
		byte[] b = new byte[buffer.readableBytes()];
		buffer.getBytes(0, b);
		String s = new String(b, GntpMessage.ENCODING);

		if (logger.isDebugEnabled()) {
			logger.debug("Message received\n{}", s);
		}
		if (dumpDir != null) {
			try {
				String fileName = "gntp-response-" + dumpCounter.getAndIncrement() + ".out";
				Files.write(b, new File(dumpDir, fileName));
			} catch (IOException e) {
				logger.warn("Could not save GNTP request dump", e);
			}
		}

		return parser.parse(s);
	}

}
