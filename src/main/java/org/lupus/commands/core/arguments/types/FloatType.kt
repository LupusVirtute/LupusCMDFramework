package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object FloatType : ArgumentType(Float::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return input[0].toFloatOrNull()
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return DoubleType.autoComplete(sender, *input)
	}
}
