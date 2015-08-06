package com.google.code.jgntp.internal.io

import com.google.code.jgntp.internal.DumpDirectory
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.internal.message.read._
import org.jboss.netty.buffer._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.oneone._
import org.slf4j._

@Sharable
class GntpMessageDecoder extends OneToOneDecoder with DumpDirectory{
  val logger: Logger = LoggerFactory.getLogger(classOf[GntpMessageDecoder])
  private final val parser: GntpMessageResponseParser = new GntpMessageResponseParser

  @throws(classOf[Exception])
  protected def decode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef): AnyRef = {
    val buffer: ChannelBuffer = msg.asInstanceOf[ChannelBuffer]
    val b: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, b)
    val s: String = new String(b, GntpMessage.ENCODING)
    if (logger.isDebugEnabled) {
      logger.debug("Message received\n{}", s)
    }
    dumpResponse(b)
    parser.parse(s)
  }
}
