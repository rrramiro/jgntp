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

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.imageio.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.write.*;
import com.google.code.jgntp.internal.util.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;

public abstract class GntpMessage {

	public static final String PROTOCOL_ID = "GNTP";
	public static final String SEPARATOR = "\r\n";
	
	public static final char HEADER_SEPARATOR = ':';

	public static final String NONE_ENCRYPTION_ALGORITHM = "NONE";
	public static final String BINARY_HASH_FUNCTION = "MD5";
	public static final Charset ENCODING = Charsets.UTF_8;
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DATE_TIME_FORMAT_ALTERNATE = "yyyy-MM-dd HH:mm:ss'Z'";
        public static final String DATE_TIME_FORMAT_GROWL_1_3 = "yyyy-MM-dd";
	public static final String IMAGE_FORMAT = "png";

	public static final String BINARY_SECTION_ID = "Identifier:";
	public static final String BINARY_SECTION_LENGTH = "Length:";

	private final GntpMessageType type;
	private final GntpPassword password;
	private final boolean encrypt;
	private final Map<String, String> headers;
	private final List<BinarySection> binarySections;
	private final DateFormat dateFormat;

	private final StringBuilder buffer;
	
	public GntpMessage(GntpMessageType type, GntpPassword password, boolean encrypt) {
		this.type = type;
		this.password = password;
		this.encrypt = encrypt;
		headers = Maps.newHashMap();
		binarySections = Lists.newArrayList();
		dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		buffer = new StringBuilder();
	}

	public abstract void append(OutputStream output) throws IOException;

	public void appendStatusLine(GntpMessageWriter writer) throws IOException {
		writer.writeStatusLine(type);
	}

	public void appendHeader(GntpMessageHeader header, Object value, GntpMessageWriter writer) throws IOException {
		appendHeader(header.toString(), value, writer);
	}

	public void appendHeader(String name, Object value, GntpMessageWriter writer) throws IOException {
		buffer.append(name).append(HEADER_SEPARATOR).append(' ');
		if (value != null) {
			if (value instanceof String) {
				String s = (String) value;
				s = s.replaceAll("\r\n", "\n");
				buffer.append(s);
			} else if (value instanceof Number) {
				buffer.append(((Number) value).toString());
			} else if (value instanceof Boolean) {
				String s = ((Boolean) value).toString();
				s = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, s);
				buffer.append(s);
			} else if (value instanceof Date) {
				String s = dateFormat.format((Date) value);
				buffer.append(s);
			} else if (value instanceof URI) {
				buffer.append(((URI) value).toString());
			} else if (value instanceof GntpId) {
				buffer.append(value.toString());
			} else if (value instanceof InputStream) {
				byte[] data = ByteStreams.toByteArray((InputStream)value);
				GntpId id = addBinary(data);
				buffer.append(id.toString());
			} else if (value instanceof byte[]) {
				byte[] data = (byte[])value;
				GntpId id = addBinary(data);
				buffer.append(id.toString());
			} else {
				throw new IllegalArgumentException("Value of header [" + name + "] not supported: " + value);
			}
		}
		writer.writeHeaderLine(buffer.toString());
		buffer.setLength(0);
	}

	public boolean appendIcon(GntpMessageHeader header, RenderedImage image, URI uri, GntpMessageWriter writer) throws IOException {
		if (image == null && uri == null) {
			return false;
		}

		if (image == null) {
			appendHeader(header, uri, writer);
		} else {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (!ImageIO.write(image, IMAGE_FORMAT, output)) {
				throw new IllegalStateException("Could not read icon data");
			}
			appendHeader(header, output.toByteArray(), writer);
		}
		return true;
	}

	public GntpId addBinary(byte[] data) throws IOException {
		BinarySection binarySection = new BinarySection(data);
		binarySections.add(binarySection);

		return GntpId.of(binarySection.getId());
	}

	public void appendBinarySections(GntpMessageWriter writer) throws IOException {
		for (Iterator<BinarySection> iter = binarySections.iterator(); iter.hasNext(); ) {
			BinarySection binarySection = iter.next();
			writer.writeBinarySection(binarySection);
			if (iter.hasNext()) {
				appendSeparator(writer);
				appendSeparator(writer);
			}
		}
	}

	public void appendSeparator(GntpMessageWriter writer) throws IOException {
		writer.writeSeparator();
	}

	public void clearBinarySections() {
		binarySections.clear();
	}

	public void putHeaders(Map<String, String> map) {
		headers.putAll(map);
	}

	public Map<String, String> getHeaders() {
		return ImmutableMap.copyOf(headers);
	}

	public List<BinarySection> getBinarySections() {
		return ImmutableList.copyOf(binarySections);
	}

	protected GntpMessageWriter getWriter(OutputStream output) {
		GntpMessageWriter messageWriter;
		if (encrypt) {
			messageWriter = new EncryptedGntpMessageWriter();
		} else {
			messageWriter = new ClearTextGntpMessageWriter();
		}
		messageWriter.prepare(output, password);
		return messageWriter;
	}

	public static class BinarySection {
		private final String id;
		private final byte[] data;

		public BinarySection(byte[] data) throws IOException {
			this.data = data;
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance(BINARY_HASH_FUNCTION);
				digest.update(data);
				byte[] digested = digest.digest();
				id = Hex.toHexadecimal(digested);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public String getId() {
			return id;
		}

		public byte[] getData() {
			return data;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id == null ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			BinarySection other = (BinarySection) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}
	}
}
