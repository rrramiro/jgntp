package com.google.code.jgntp.internal.nio

import java.io._
import java.nio.file.{Paths, Files}
import java.util
import java.util.concurrent.atomic._

import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.internal.message.read._
import org.slf4j._

import io.netty.buffer._
import io.netty.channel.ChannelHandler._
import io.netty.channel._
import io.netty.handler.codec._


@Sharable
 object GntpMessageDecoder {
  val LOGGER_NAME: String = "jgntp.message"
  private val logger: Logger = LoggerFactory.getLogger(LOGGER_NAME)
  private val DUMP_MESSAGES_DIRECTORY_PROPERTY: String = "gntp.response.dump.dir"
}

@Sharable
class GntpMessageDecoder extends MessageToMessageDecoder[ByteBuf] {
  val dumpDirName: String = System.getProperty(GntpMessageDecoder.DUMP_MESSAGES_DIRECTORY_PROPERTY)
  private final val parser: GntpMessageResponseParser = new GntpMessageResponseParser
  private var dumpDir: File = if (dumpDirName == null) null else new File(dumpDirName)

  private var dumpCounter: AtomicLong = null
  if (dumpDir != null) {
    dumpCounter = new AtomicLong
    try {
      dumpDir.mkdirs()
    }
    catch {
      case e: IOException => {
        GntpMessageDecoder.logger.warn("Could not get/create GNTP response dump directory, dumping will be disabled", e)
        dumpDir = null
      }
    }
  }


  @throws(classOf[Exception])
  override def decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: util.List[AnyRef]): Unit = {
    val buffer: ByteBuf = msg
    val b: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, b)
    val s: String = new String(b, GntpMessage.ENCODING)
    if (GntpMessageDecoder.logger.isDebugEnabled) {
      GntpMessageDecoder.logger.debug("Message received\n{}", s)
    }
    if (dumpDir != null) {
      try {
        val fileName: String = "gntp-response-" + dumpCounter.getAndIncrement + ".out"
        Files.write(Paths.get(new File(dumpDir, fileName).toURI), b)
      }
      catch {
        case e: IOException =>
          GntpMessageDecoder.logger.warn("Could not save GNTP request dump", e)
      }
    }
    parser.parse(s)
  }
}
