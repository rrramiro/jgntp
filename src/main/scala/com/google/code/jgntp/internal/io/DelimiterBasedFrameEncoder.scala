package com.google.code.jgntp.internal.io

import java.io._
import java.nio.file.{Paths, Path, Files}
import java.util.concurrent.atomic._

import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.buffer._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.oneone._
import org.slf4j._

@Sharable object DelimiterBasedFrameEncoder {
  private val DUMP_MESSAGES_DIRECTORY_PROPERTY: String = "gntp.request.dump.dir"
  private val logger: Logger = LoggerFactory.getLogger(classOf[DelimiterBasedFrameEncoder])
}

@Sharable class DelimiterBasedFrameEncoder(delimiterSource: ChannelBuffer) extends OneToOneEncoder {
  private val delimiter: ChannelBuffer = copiedBuffer(delimiterSource)
  val dumpDirName: String = System.getProperty(DelimiterBasedFrameEncoder.DUMP_MESSAGES_DIRECTORY_PROPERTY)

  private var dumpDir: File = null
  private var dumpCounter: AtomicLong = null

  dumpDir = if (dumpDirName == null) null else new File(dumpDirName)
  if (dumpDir != null) {
    dumpCounter = new AtomicLong
    try {
      dumpDir.mkdirs()
    }
    catch {
      case e: IOException => {
        DelimiterBasedFrameEncoder.logger.warn("Could not get/create GNTP request dump directory, dumping will be disabled", e)
        dumpDir = null
      }
    }
  }


  @throws(classOf[Exception])
  protected def encode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef): AnyRef = {
    if (!msg.isInstanceOf[ChannelBuffer]) {
      return msg
    }
    val buffer: ChannelBuffer = copiedBuffer(msg.asInstanceOf[ChannelBuffer], delimiter)
    if (dumpDir != null) {
      try {
        val fileName: String = "gntp-request-" + dumpCounter.getAndIncrement + ".out"
        val b: Array[Byte] = new Array[Byte](buffer.readableBytes)
        buffer.getBytes(0, b)
        Files.write(Paths.get(new File(dumpDir, fileName).toURI), b)
      }
      catch {
        case e: IOException => {
          DelimiterBasedFrameEncoder.logger.warn("Could not save GNTP request dump", e)
        }
      }
    }
    buffer
  }
}

