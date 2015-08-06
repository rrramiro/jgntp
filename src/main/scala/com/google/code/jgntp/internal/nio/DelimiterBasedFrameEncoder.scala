package com.google.code.jgntp.internal.nio

import com.google.code.jgntp.internal.DumpDirectory
import com.google.code.jgntp.internal.message.GntpMessage
import io.netty.channel.ChannelHandler._
import io.netty.buffer._
import io.netty.handler.codec._
import io.netty.channel._
import org.slf4j._




@Sharable class DelimiterBasedFrameEncoder(delimiterSource: ByteBuf) extends MessageToMessageEncoder[ByteBuf] with DumpDirectory {
  val logger: Logger = LoggerFactory.getLogger(classOf[DelimiterBasedFrameEncoder])
  private val delimiter: ByteBuf = Unpooled.copiedBuffer(delimiterSource)

  @throws(classOf[Exception])
  protected def encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: java.util.List[AnyRef]): Unit = {
    val buffer: ByteBuf = Unpooled.copiedBuffer(msg, delimiter)
    val bufferArray: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, bufferArray)
    dumpBuffer(bufferArray)
    out.add(msg.toString(GntpMessage.ENCODING))
  }
}

