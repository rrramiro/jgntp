package com.google.code.jgntp.internal.nio

import java.io._

import com.google.code.jgntp.internal.message._
import io.netty.handler.codec.MessageToByteEncoder
import org.apache.commons.io.output.TeeOutputStream
import org.slf4j._

import io.netty.buffer._
import io.netty.channel.ChannelHandler._
import io.netty.channel._

@Sharable object GntpMessageEncoder {
  private val logger: Logger = LoggerFactory.getLogger(GntpMessageDecoder.LOGGER_NAME)
}

@Sharable class GntpMessageEncoder extends MessageToByteEncoder[GntpMessageRequest] {

  @throws(classOf[Exception])
  override def encode(ctx: ChannelHandlerContext, msg: GntpMessageRequest, out: ByteBuf): Unit = {
    var debugOutputStream: ByteArrayOutputStream = null
    val outputStream: OutputStream = if (GntpMessageEncoder.logger.isDebugEnabled) {
      debugOutputStream = new ByteArrayOutputStream
      new TeeOutputStream(new ByteBufOutputStream(out), debugOutputStream)
    }
    else {
      debugOutputStream = null
      new ByteBufOutputStream(out)
    }
    msg.append(outputStream)

    if (GntpMessageEncoder.logger.isDebugEnabled) {
      GntpMessageEncoder.logger.debug("Sending message\n{}", new String(debugOutputStream.toByteArray, GntpMessage.ENCODING))
    }

  }
}
