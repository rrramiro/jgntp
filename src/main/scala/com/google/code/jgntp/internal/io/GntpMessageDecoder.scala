package com.google.code.jgntp.internal.io

import java.io._
import java.util.concurrent.atomic._
import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.channel.ChannelHandler._
import org.jboss.netty.handler.codec.oneone._
import org.slf4j._
import com.google.code.jgntp.internal.message._
import com.google.code.jgntp.internal.message.read._
import com.google.common.io._

@Sharable
 object GntpMessageDecoder {
  val LOGGER_NAME: String = "jgntp.message"
  private val logger: Logger = LoggerFactory.getLogger(LOGGER_NAME)
  private val DUMP_MESSAGES_DIRECTORY_PROPERTY: String = "gntp.response.dump.dir"
}

@Sharable
class GntpMessageDecoder extends OneToOneDecoder {
  val dumpDirName: String = System.getProperty(GntpMessageDecoder.DUMP_MESSAGES_DIRECTORY_PROPERTY)
  private final val parser: GntpMessageResponseParser = new GntpMessageResponseParser
  private var dumpDir: File = if (dumpDirName == null) null else new File(dumpDirName)

  private var dumpCounter: AtomicLong = null
  if (dumpDir != null) {
    dumpCounter = new AtomicLong
    try {
      Files.createParentDirs(dumpDir)
    }
    catch {
      case e: IOException => {
        GntpMessageDecoder.logger.warn("Could not get/create GNTP response dump directory, dumping will be disabled", e)
        dumpDir = null
      }
    }
  }


  @throws(classOf[Exception])
  protected def decode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef): AnyRef = {
    val buffer: ChannelBuffer = msg.asInstanceOf[ChannelBuffer]
    val b: Array[Byte] = new Array[Byte](buffer.readableBytes)
    buffer.getBytes(0, b)
    val s: String = new String(b, GntpMessage.ENCODING)
    if (GntpMessageDecoder.logger.isDebugEnabled) {
      GntpMessageDecoder.logger.debug("Message received\n{}", s)
    }
    if (dumpDir != null) {
      try {
        val fileName: String = "gntp-response-" + dumpCounter.getAndIncrement + ".out"
        Files.write(b, new File(dumpDir, fileName))
      }
      catch {
        case e: IOException => {
          GntpMessageDecoder.logger.warn("Could not save GNTP request dump", e)
        }
      }
    }
    return parser.parse(s)
  }
}
