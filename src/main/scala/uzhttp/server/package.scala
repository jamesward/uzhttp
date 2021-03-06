package uzhttp

import java.nio.channels.SelectionKey

import zio.duration.Duration
import zio.{Has, URIO, ZIO}

package object server {

  type Logging = Has[ServerLogger[Any]]

  object Logging {
    def info(str: => String): URIO[Logging, Unit] = ZIO.accessM[Logging](_.get[ServerLogger[Any]].info(str))
    def request(req: Request, rep: Response, startDuration: Duration, finishDuration: Duration): URIO[Logging, Unit] = ZIO.accessM[Logging](_.get[ServerLogger[Any]].request(req, rep, startDuration, finishDuration))
    def debug(str: => String): URIO[Logging, Unit] = ZIO.accessM[Logging](_.get[ServerLogger[Any]].debug(str))
    def error(str: String, err: Throwable): URIO[Logging, Unit] = ZIO.accessM[Logging](_.get[ServerLogger[Any]].error(str, err))
  }

  private[server] val EmptyLine: Array[Byte] = CRLF ++ CRLF

  // The most copy-pasted StackOverflow snippet of all time, adapted to unprincipled Scala!
  private[server] def humanReadableByteCountSI(bytes: Long): String = {
    val s = if (bytes < 0) "-" else ""
    var b = if (bytes == Long.MinValue) Long.MaxValue else Math.abs(bytes)
    if (b < 1000L) return bytes.toString + " B"
    if (b < 999950L) return "%s%.1f kB".format(s, b / 1e3)
    b /= 1000
    if (b < 999950L) return "%s%.1f MB".format(s, b / 1e3)
    b /= 1000
    if (b < 999950L) return "%s%.1f GB".format(s, b / 1e3)
    b /= 1000

    "%s%.1f TB".format(s, b / 1e3)
  }

  private[server] implicit class IterateKeys(val self: java.util.Set[SelectionKey]) extends AnyVal {
    def toIterable: Iterable[SelectionKey] = new Iterable[SelectionKey] {
      override def iterator: Iterator[SelectionKey] = {
        val jIterator = self.iterator()
        new Iterator[SelectionKey] {
          override def hasNext: Boolean = jIterator.hasNext
          override def next(): SelectionKey = jIterator.next()
        }
      }
    }
  }
}
