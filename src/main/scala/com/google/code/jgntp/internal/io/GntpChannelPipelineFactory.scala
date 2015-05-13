package com.google.code.jgntp.internal.io

import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame._

class GntpChannelPipelineFactory(handler: ChannelHandler) extends ChannelPipelineFactory {
  private final val delimiterEncoder: ChannelHandler = new DelimiterBasedFrameEncoder(getDelimiter)
  private final val messageDecoder: ChannelHandler = new GntpMessageDecoder
  private final val messageEncoder: ChannelHandler = new GntpMessageEncoder

  @throws(classOf[Exception])
  def getPipeline: ChannelPipeline = {
    val pipeline: ChannelPipeline = Channels.pipeline
    pipeline.addLast("delimiter-decoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, getDelimiter))
    pipeline.addLast("delimiter-encoder", delimiterEncoder)
    pipeline.addLast("message-decoder", messageDecoder)
    pipeline.addLast("message-encoder", messageEncoder)
    pipeline.addLast("handler", handler)
    pipeline
  }

  protected def getDelimiter: ChannelBuffer = {
    ChannelBuffers.wrappedBuffer(Array[Byte]('\r'.toByte, '\n'.toByte, '\r'.toByte, '\n'.toByte))
  }
}
