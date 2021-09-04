package com.lupus.example

import org.bukkit.command.CommandSender
import org.lupus.commands.core.annotations.clazz.Continuous

@Continuous
class AnotherTestCMD(val test: Int) {
    fun run(executor: CommandSender): String {
        return "<red>xD $test"
    }
}