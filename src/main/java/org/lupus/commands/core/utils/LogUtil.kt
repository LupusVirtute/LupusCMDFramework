package org.lupus.commands.core.utils

import org.bukkit.Bukkit
import java.util.logging.Level

object LogUtil {
    fun outMsg(string: String, level: Level) {
        Bukkit.getLogger().log(level, string)
    }
    fun outMsg(string: String) {
        outMsg(string, Level.INFO)
    }
}