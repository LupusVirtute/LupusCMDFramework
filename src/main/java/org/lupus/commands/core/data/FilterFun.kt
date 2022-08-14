package org.lupus.commands.core.data

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

abstract class FilterFun {
    abstract fun filterTabComplete(sender: CommandSender, commandLupi: CommandLupi, argType: ArgumentType, tabCompleteResult: MutableList<String>): MutableList<String>
}