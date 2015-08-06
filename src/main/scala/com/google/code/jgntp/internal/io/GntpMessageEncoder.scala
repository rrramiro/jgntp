package com.google.code.jgntp.internal.io

import java.io._

import com.google.code.jgntp.internal.message._
import org.jboss.netty.buffer._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.channel._
import org.slf4j._



@Sharable class GntpMessageEncoder extends SimpleChannelHandler {
  val logger: Logger = LoggerFactory.getLogger(classOf[GntpMessageEncoder])

  @throws(classOf[Exception])
  override def writeRequested(ctx: ChannelHandlerContext, e: MessageEvent) {
    val message: GntpMessageRequest = e.getMessage.asInstanceOf[GntpMessageRequest]
    val buffer = new ByteArrayOutputStream
    message.append(buffer)
    if (logger.isDebugEnabled) {
      logger.debug("Sending message\n{}", new String(buffer.toByteArray, GntpMessage.ENCODING))
    }
    Channels.write(ctx, e.getFuture, ChannelBuffers.copiedBuffer(buffer.toByteArray), e.getRemoteAddress)
  }
}
