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
import java.net.*;

import org.jboss.netty.channel.*;
import org.slf4j.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;

public class GntpChannelHandler extends SimpleChannelUpstreamHandler {

	private Logger logger = LoggerFactory.getLogger(GntpChannelHandler.class);

	private final NioGntpClient gntpClient;
	private final GntpListener listener;

	public GntpChannelHandler(NioGntpClient gntpClient, GntpListener listener) {
		this.gntpClient = gntpClient;
		this.listener = listener;
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("Channel closed [{}]", e.getChannel());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		GntpMessageResponse message = (GntpMessageResponse) e.getMessage();
		handleMessage(message);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		try {
			Throwable cause = e.getCause();
			if (gntpClient.isRegistered()) {
				handleIOError(cause);
			} else {
				if (cause instanceof ConnectException) {
					handleIOError(cause);
					gntpClient.retryRegistration();
				} else if (cause instanceof IOException) {
					// This code is necessary until Grow for Windows version 2.0.4, later versions should not need it
					// GfW is closing connections prematurely sometimes
					// IOException is thrown with the message "An existing connection was forcibly closed by the remote host"
					// so just assume we registered successfully if we didn't get a ConnectException during registration
					// See discussion about it here: http://groups.google.com/group/growl-for-windows/browse_frm/thread/2523afc60296594
					handleMessage(new GntpOkMessage(-1, GntpMessageType.REGISTER, null));
				} else {
					handleIOError(cause);
				}
			}
		} finally {
			e.getChannel().close();
		}
	}

	protected void handleMessage(GntpMessageResponse message) {
		Preconditions.checkState(message instanceof GntpOkMessage || message instanceof GntpCallbackMessage || message instanceof GntpErrorMessage);
                logger.debug("handling message...");
		if (gntpClient.isRegistered()) {
                        GntpNotification notification = (GntpNotification)gntpClient.getNotificationsSent().get(message.getInternalNotificationId());
			if (notification != null) {
				if (message instanceof GntpOkMessage) {
                                        logger.debug("OK - message.");
					try {
						if (listener != null) {
							listener.onNotificationSuccess(notification);
						}
					} finally {
						if (!notification.isCallbackRequested()) {
							gntpClient.getNotificationsSent().remove(message.getInternalNotificationId());
						}
					}
				} else if (message instanceof GntpCallbackMessage) {
					logger.debug("Callback - message.");
					gntpClient.getNotificationsSent().remove(message.getInternalNotificationId());
					if (listener == null) {
						throw new IllegalStateException("A GntpListener must be set in GntpClient to be able to receive callbacks");
					}
					GntpCallbackMessage callbackMessage = (GntpCallbackMessage) message;
					switch (callbackMessage.getCallbackResult()) {
						case CLICK:
							listener.onClickCallback(notification);
							break;
						case CLOSE:
							listener.onCloseCallback(notification);
							break;
						case TIMEOUT:
							listener.onTimeoutCallback(notification);
							break;
						default:
							throw new IllegalStateException("Unknown callback result: " + callbackMessage.getCallbackResult());
					}
				} else if (message instanceof GntpErrorMessage) {
					logger.debug("ERROR - message.");
					GntpErrorMessage errorMessage = (GntpErrorMessage) message;
					if (listener != null) {
						listener.onNotificationError(notification, errorMessage.getStatus(), errorMessage.getDescription());
					}
					if (GntpErrorStatus.UNKNOWN_APPLICATION == errorMessage.getStatus() ||
						GntpErrorStatus.UNKNOWN_NOTIFICATION == errorMessage.getStatus()) {
						gntpClient.retryRegistration();
					}
				} else {
                                        logger.warn("Unknown message type. [{}]", message);
                                }
			} else {
                            logger.debug("notification is null. Not much we can do now...");
                        }
		} else {
			logger.debug("application not registered. Not much we can do.");
                        if (message instanceof GntpOkMessage) {
				try {
					if (listener != null) {
						listener.onRegistrationSuccess();
					}
				} finally {
					gntpClient.setRegistered();
				}
			} else if (message instanceof GntpErrorMessage) {
				GntpErrorMessage errorMessage = (GntpErrorMessage) message;
				if (listener != null) {
					listener.onRegistrationError(errorMessage.getStatus(), errorMessage.getDescription());
				}
				if (GntpErrorStatus.NOT_AUTHORIZED == errorMessage.getStatus()) {
					gntpClient.retryRegistration();
				}
			}
		}
	}
	
	protected void handleIOError(Throwable t) {
		if (listener == null) {
			logger.error("Error in GNTP I/O operation", t);
		} else {
			listener.onCommunicationError(t);
		}
	}
}
