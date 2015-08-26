package com.google.code.jgntp.internal.io

import java.io._

import com.google.code.jgntp.internal.message._
import io.netty.buffer._
import io.netty.channel.ChannelHandler._
import io.netty.channel._
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j._


@Sharable class GntpMessageEncoder extends MessageToByteEncoder[GntpMessageRequest] {
  val logger: Logger = LoggerFactory.getLogger(classOf[GntpMessageEncoder])

  @throws(classOf[Exception])
  override def encode(ctx: ChannelHandlerContext, msg: GntpMessageRequest, out: ByteBuf): Unit = {
    val outBuffer = new ByteArrayOutputStream
    msg.append(outBuffer)
    //outBuffer.write(GntpMessage.SEPARATOR.getBytes)
    //outBuffer.write(GntpMessage.SEPARATOR.getBytes)
    if (logger.isDebugEnabled) {
      logger.debug("Sending message\n{}", new String(outBuffer.toByteArray, GntpMessage.ENCODING))
      //logger.debug("Sending message\n{}", "XX")
    }
    //out.add(Unpooled.wrappedBuffer(outBuffer.toByteArray))
    out.writeBytes(outBuffer.toByteArray)

  }
}
