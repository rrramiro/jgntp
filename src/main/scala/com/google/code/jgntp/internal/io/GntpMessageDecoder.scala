package com.google.code.jgntp.internal.io

import java.util

import com.google.code.jgntp.internal.DumpDirectory
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.internal.message.read._
import io.netty.buffer._
import io.netty.channel.ChannelHandler._
import io.netty.channel._
import io.netty.handler.codec._
import org.slf4j._


@Sharable
class GntpMessageDecoder extends MessageToMessageDecoder[ByteBuf] with DumpDirectory {
  val logger: Logger = LoggerFactory.getLogger(classOf[GntpMessageDecoder])
  private final val parser: GntpMessageResponseParser = new GntpMessageResponseParser

  @throws(classOf[Exception])
  override def decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: util.List[AnyRef]): Unit = {
    val buffer: ByteBuf = msg
    val b: Array[Byte] = new Array[Byte](buffer.readableBytes)
//    buffer.getBytes(0, b)
    buffer.readBytes(b)
    val s: String = new String(b, GntpMessage.ENCODING)
    if (logger.isDebugEnabled) {
      logger.debug("Message received\n{}", s)
    }
    dumpResponse(b)
    val response = parser.parse(s)
    out.add(response)
  }
}
