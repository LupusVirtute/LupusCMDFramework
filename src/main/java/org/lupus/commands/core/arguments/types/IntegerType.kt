package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType
import java.util.*

object IntegerType : ArgumentType(Int::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any {
		return input[0].toInt()
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return mutableListOf("-1", "0", "1", "2", "3")
	}
}