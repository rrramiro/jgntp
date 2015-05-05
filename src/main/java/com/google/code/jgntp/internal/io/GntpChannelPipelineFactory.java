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

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.*;

public class GntpChannelPipelineFactory implements ChannelPipelineFactory {

	private final ChannelHandler delimiterEncoder;
	private final ChannelHandler messageDecoder;
	private final ChannelHandler messageEncoder;
	private final ChannelHandler handler;

	public GntpChannelPipelineFactory(ChannelHandler handler) {
		delimiterEncoder = new DelimiterBasedFrameEncoder(getDelimiter());
		messageDecoder = new GntpMessageDecoder();
		messageEncoder = new GntpMessageEncoder();
		this.handler = handler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("delimiter-decoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, getDelimiter()));
		pipeline.addLast("delimiter-encoder", delimiterEncoder);
		pipeline.addLast("message-decoder", messageDecoder);
		pipeline.addLast("message-encoder", messageEncoder);
		pipeline.addLast("handler", handler);

		return pipeline;
	}

	protected ChannelBuffer getDelimiter() {
		return ChannelBuffers.wrappedBuffer(new byte[] { (byte) '\r', (byte) '\n', (byte) '\r', (byte) '\n' });
	}
}
