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
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.jboss.netty.buffer.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.code.jgntp.internal.message.GntpMessage.*;
import com.google.code.jgntp.internal.util.*;

public class EncryptedGntpMessageWriter extends AbstractGntpMessageWriter {

	public static final String DEFAULT_ALGORITHM = "DES";
	public static final String DEFAULT_TRANSFORMATION = "DES/CBC/PKCS5Padding";

	private Cipher cipher;
	private SecretKey secretKey;
	private IvParameterSpec iv;
	private ChannelBuffer buffer;

	@Override
	public void prepare(OutputStream outputStream, GntpPassword gntpPassword) {
		super.prepare(outputStream, gntpPassword);
		buffer = ChannelBuffers.dynamicBuffer();
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);
			secretKey = keyFactory.generateSecret(new DESKeySpec(gntpPassword.getKey()));
			iv = new IvParameterSpec(secretKey.getEncoded());

			cipher = Cipher.getInstance(DEFAULT_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void writeEncryptionSpec() throws IOException {
		writer.append(DEFAULT_ALGORITHM).append(':').append(Hex.toHexadecimal(iv.getIV()));
	}

	@Override
	public void startHeaders() throws IOException {
		super.startHeaders();
		writer = new OutputStreamWriter(new ChannelBufferOutputStream(buffer), GntpMessage.ENCODING);
	}

	@Override
	public void finishHeaders() throws IOException {
		super.finishHeaders();
		byte[] headerData = new byte[buffer.readableBytes()];
		buffer.getBytes(0, headerData);
		byte[] encryptedHeaderData = encrypt(headerData);
		output.write(encryptedHeaderData);
		
		writer = new OutputStreamWriter(output, GntpMessage.ENCODING);
		writeSeparator();
		writeSeparator();
	}

	@Override
	protected byte[] getDataForBinarySection(BinarySection binarySection) {
		return encrypt(binarySection.getData());
	}

	protected byte[] encrypt(byte[] data) {
		try {
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
