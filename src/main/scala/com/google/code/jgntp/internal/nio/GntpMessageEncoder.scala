package com.google.code.jgntp.internal.nio

import java.io._

import com.google.code.jgntp.internal.message._
import io.netty.handler.codec.MessageToMessageEncoder
import org.slf4j._

import io.netty.buffer._
import io.netty.channel.ChannelHandler._
import io.netty.channel._


@Sharable class GntpMessageEncoder extends MessageToMessageEncoder[GntpMessageRequest] {
  val logger: Logger = LoggerFactory.getLogger(classOf[GntpMessageEncoder])
  @throws(classOf[Exception])
  override def encode(ctx: ChannelHandlerContext, msg: GntpMessageRequest, out: java.util.List[AnyRef]): Unit = {
    val outBuffer = new ByteArrayOutputStream
    msg.append(outBuffer)
    //outBuffer.write(GntpMessage.SEPARATOR.getBytes)
    //outBuffer.write(GntpMessage.SEPARATOR.getBytes)
    if (logger.isDebugEnabled) {
      logger.debug("Sending message\n{}", new String(outBuffer.toByteArray, GntpMessage.ENCODING))
    }
    out.add(Unpooled.wrappedBuffer(outBuffer.toByteArray))
  }
}
