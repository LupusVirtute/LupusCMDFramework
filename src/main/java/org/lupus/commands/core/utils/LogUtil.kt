package org.lupus.commands.core.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager.getLogger
import org.apache.logging.log4j.core.appender.FileAppender

object LogUtil {
    var debug: Boolean = false
    val logger = getLogger("LCF")

    init {
        val fA = FileAppender.newBuilder().withFileName("lcf-debug.log").setName("LCF").build()
        fA.start()
    }

    fun outMsg(string: String, level: Level) {
        logger.log(level, string)
    }
    fun outMsg(string: String) {
        if (!debug)
            return
        outMsg(string, Level.INFO)
    }
}