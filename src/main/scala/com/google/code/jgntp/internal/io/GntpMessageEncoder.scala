package com.google.code.jgntp.internal.io

import java.io._

import com.google.code.jgntp.internal.message._
import org.apache.commons.io.output.TeeOutputStream
import org.jboss.netty.buffer._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.channel._
import org.slf4j._

@Sharable object GntpMessageEncoder {
  private val logger: Logger = LoggerFactory.getLogger(GntpMessageDecoder.LOGGER_NAME)
}

@Sharable class GntpMessageEncoder extends SimpleChannelHandler {
  @SuppressWarnings(Array("null"))
  @throws(classOf[Exception])
  override def writeRequested(ctx: ChannelHandlerContext, e: MessageEvent) {
    val message: GntpMessageRequest = e.getMessage.asInstanceOf[GntpMessageRequest]
    val buffer: ChannelBuffer = ChannelBuffers.dynamicBuffer
    var outputStream: OutputStream = null
    var debugOutputStream: ByteArrayOutputStream = null
    if (GntpMessageEncoder.logger.isDebugEnabled) {
      debugOutputStream = new ByteArrayOutputStream
      outputStream = new TeeOutputStream(new ChannelBufferOutputStream(buffer), debugOutputStream)
    }
    else {
      debugOutputStream = null
      outputStream = new ChannelBufferOutputStream(buffer)
    }
    message.append(outputStream)
    if (GntpMessageEncoder.logger.isDebugEnabled) {
      GntpMessageEncoder.logger.debug("Sending message\n{}", new String(debugOutputStream.toByteArray, GntpMessage.ENCODING))
    }
    Channels.write(ctx, e.getFuture, buffer, e.getRemoteAddress)
  }
}
