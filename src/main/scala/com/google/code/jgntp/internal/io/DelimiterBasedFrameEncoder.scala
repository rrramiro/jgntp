package com.google.code.jgntp.internal.io


import com.google.code.jgntp.internal.DumpDirectory
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.buffer._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.oneone._
import org.slf4j._


@Sharable
class DelimiterBasedFrameEncoder(delimiterSource: ChannelBuffer) extends OneToOneEncoder with DumpDirectory{
  private val delimiter: ChannelBuffer = copiedBuffer(delimiterSource)
  val logger: Logger = LoggerFactory.getLogger(classOf[DelimiterBasedFrameEncoder])

  @throws(classOf[Exception])
  protected def encode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef): AnyRef = {
    if (!msg.isInstanceOf[ChannelBuffer]) {
      return msg
    }
    val buffer: ChannelBuffer = copiedBuffer(msg.asInstanceOf[ChannelBuffer], delimiter)
    val bufferArray: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, buffer)
    dumpBuffer(bufferArray)
    buffer
  }
}

