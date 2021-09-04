package com.lupus.example

import org.bukkit.command.CommandSender
import org.lupus.commands.core.annotations.CMDPass
import org.lupus.commands.core.annotations.clazz.Continuous

@Continuous
class TestCMD {
    @CMDPass("com.lupus.example.AnotherTestCMD")
    fun test(executor: CommandSender, lol: Int): String {
        return "<red> Lol xD $lol"
    }
}