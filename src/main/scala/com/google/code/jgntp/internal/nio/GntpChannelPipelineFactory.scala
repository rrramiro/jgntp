package com.google.code.jgntp.internal.nio


import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec._
import io.netty.channel.socket.SocketChannel

object GntpChannelPipelineFactory{
  def getDelimiter: ByteBuf = Unpooled.wrappedBuffer(Array[Byte]('\r'.toByte, '\n'.toByte, '\r'.toByte, '\n'.toByte))
}

class GntpChannelPipelineFactory(handler: GntpChannelHandler) extends ChannelInitializer[SocketChannel] {

  @Override
  def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()
    pipeline.addLast("delimiter-decoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, GntpChannelPipelineFactory.getDelimiter))
    pipeline.addLast("delimiter-encoder", new DelimiterBasedFrameEncoder(GntpChannelPipelineFactory.getDelimiter))
    pipeline.addLast("message-decoder", new GntpMessageDecoder)
    pipeline.addLast("message-encoder", new GntpMessageEncoder)
    pipeline.addLast("handler", handler)

  }



}
