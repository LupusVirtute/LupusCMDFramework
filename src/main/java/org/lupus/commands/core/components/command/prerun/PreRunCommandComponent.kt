package org.lupus.commands.core.components.command.prerun

import org.bukkit.command.CommandSender
import org.lupus.commands.core.components.command.CommandComponent
import org.lupus.commands.core.data.CommandLupi

abstract class PreRunCommandComponent(command: CommandLupi) : CommandComponent(command) {
    abstract fun run(sender: CommandSender, args: Array<out String>, commandObj: Any)
    var async = false
    var subCommand: CommandLupi? = null
    var cmdParams: MutableList<Any>? = null
}