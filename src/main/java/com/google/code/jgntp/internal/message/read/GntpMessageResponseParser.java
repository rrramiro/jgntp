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
package com.google.code.jgntp.internal.message.read;

import java.text.*;
import java.util.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class GntpMessageResponseParser {

	private Splitter separatorSplitter;
	private Splitter statusLineSplitter;

	public GntpMessageResponseParser() {
		separatorSplitter = Splitter.on(GntpMessage.SEPARATOR).omitEmptyStrings();
		statusLineSplitter = Splitter.on(' ').omitEmptyStrings().trimResults();
	}

	public GntpMessageResponse parse(String s) {
		Iterable<String> splitted = separatorSplitter.split(s);
		Preconditions.checkState(!Iterables.isEmpty(splitted), "Empty message received from Growl");

		Iterator<String> iter = splitted.iterator();
		String statusLine = iter.next();
		Preconditions.checkState(statusLine.startsWith(GntpMessage.PROTOCOL_ID + "/" + GntpVersion.ONE_DOT_ZERO.toString()), "Unknown protocol version");

		Iterable<String> statusLineIterable = statusLineSplitter.split(statusLine);
		String messageTypeText = Iterables.get(statusLineIterable, 1).substring(1);
		GntpMessageType messageType = GntpMessageType.valueOf(messageTypeText);

		Map<String, String> headers = Maps.newHashMap();
		while (iter.hasNext()) {
			String[] splittedHeader = iter.next().split(":", 2);
			headers.put(splittedHeader[0], splittedHeader[1].trim());
		}

		switch (messageType) {
			case OK:
				return createOkMessage(headers);
			case CALLBACK:
				return createCallbackMessage(headers);
			case ERROR:
				return createErrorMessage(headers);
			default:
				throw new IllegalStateException("Unknown response message type: " + messageType);
		}
	}

	protected GntpOkMessage createOkMessage(Map<String, String> headers) {
		String notificationId = GntpMessageHeader.NOTIFICATION_ID.getValueInMap(headers);
		return new GntpOkMessage(getInternalNotificationId(headers), getRespondingType(headers), notificationId);
	}

	protected GntpCallbackMessage createCallbackMessage(Map<String, String> headers) {
		String notificationId = GntpMessageHeader.NOTIFICATION_ID.getValueInMap(headers);
		String callbackResultText = GntpMessageHeader.NOTIFICATION_CALLBACK_RESULT.getRequiredValueInMap(headers);
		GntpCallbackResult callbackResult = GntpCallbackResult.parse(callbackResultText);
		String context = GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT.getRequiredValueInMap(headers);
		String contextType = GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT_TYPE.getRequiredValueInMap(headers);
		String timestampText = GntpMessageHeader.NOTIFICATION_CALLBACK_TIMESTAMP.getRequiredValueInMap(headers);
		Date timestamp = null;
		try {
			timestamp = new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT).parse(timestampText);
		} catch (ParseException e) {
			try {
				timestamp = new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT_ALTERNATE).parse(timestampText);
			} catch (ParseException e1) {
				try {
                                        timestamp = new SimpleDateFormat(GntpMessage.DATE_TIME_FORMAT_GROWL_1_3).parse(timestampText);
                                } catch (ParseException e2) {
                                        throw new RuntimeException(e);
                                }
			}
		}

		return new GntpCallbackMessage(getInternalNotificationId(headers), notificationId, callbackResult, context, contextType, timestamp);
	}

	protected GntpErrorMessage createErrorMessage(Map<String, String> headers) {
		String code = GntpMessageHeader.ERROR_CODE.getRequiredValueInMap(headers);
		String description = GntpMessageHeader.ERROR_DESCRIPTION.getRequiredValueInMap(headers);
		GntpErrorStatus errorStatus = GntpErrorStatus.parse(code);
		return new GntpErrorMessage(getInternalNotificationId(headers), getRespondingType(headers), errorStatus, description);
	}

	protected long getInternalNotificationId(Map<String, String> headers) {
		String value = GntpMessageHeader.NOTIFICATION_INTERNAL_ID.getValueInMap(headers);
		return value == null ? -1 : Long.parseLong(value);
	}

	protected GntpMessageType getRespondingType(Map<String, String> headers) {
		String respondingTypeName = GntpMessageHeader.RESPONSE_ACTION.getValueInMap(headers);
		return respondingTypeName == null ? null : GntpMessageType.valueOf(respondingTypeName);
	}
}
