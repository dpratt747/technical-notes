package io.github.dpratt747.technical_notes.domain

import org.apache.commons.logging.impl.Log4JLogger

trait WithLogger {
  val log = new Log4JLogger(this.getClass.getSimpleName)
}
