package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object UIntegerType : ArgumentType(UInt::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		val value = input[0].toIntOrNull()
		return if(value == null || value > 0) value else 0
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return mutableListOf("0","1","2","3")
	}
}