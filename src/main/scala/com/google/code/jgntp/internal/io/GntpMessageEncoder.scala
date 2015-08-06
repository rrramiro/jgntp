package com.google.code.jgntp.internal.io

import java.io._

import com.google.code.jgntp.internal.message._
import org.apache.commons.io.output.TeeOutputStream
import org.jboss.netty.buffer._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.channel._
import org.slf4j._



@Sharable class GntpMessageEncoder extends SimpleChannelHandler {
  val logger: Logger = LoggerFactory.getLogger(classOf[GntpMessageEncoder])

  @throws(classOf[Exception])
  override def writeRequested(ctx: ChannelHandlerContext, e: MessageEvent) {
    val message: GntpMessageRequest = e.getMessage.asInstanceOf[GntpMessageRequest]
    val buffer: ChannelBuffer = ChannelBuffers.dynamicBuffer
    if (logger.isDebugEnabled) {
      val debugOutputStream = new ByteArrayOutputStream
      message.append(new TeeOutputStream(new ChannelBufferOutputStream(buffer), debugOutputStream))
      logger.debug("Sending message\n{}", new String(debugOutputStream.toByteArray, GntpMessage.ENCODING))
    } else {
      message.append(new ChannelBufferOutputStream(buffer))
    }
    Channels.write(ctx, e.getFuture, buffer, e.getRemoteAddress)
  }
}
