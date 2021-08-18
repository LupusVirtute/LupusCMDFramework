package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType
import java.util.*

object DoubleType : ArgumentType(Double::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any {
		return input[0].toDouble()
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return mutableListOf("1.23", "2.42")
	}
}