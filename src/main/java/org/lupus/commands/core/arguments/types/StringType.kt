package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object StringType : ArgumentType(String::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any {
		return input[0]
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return mutableListOf(" ")
	}
}