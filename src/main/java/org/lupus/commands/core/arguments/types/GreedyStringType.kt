package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object GreedyStringType : ArgumentType(Array<out String>::class.java, -1) {
	override fun conversion(sender: CommandSender, vararg input: String): Any {
		return input
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return mutableListOf(" ")
	}
}