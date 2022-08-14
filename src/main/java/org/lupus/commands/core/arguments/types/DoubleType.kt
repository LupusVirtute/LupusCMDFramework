package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType
import java.util.*

object DoubleType : ArgumentType(Double::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return input[0].toDoubleOrNull()
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val number = input[0].toDoubleOrNull() ?: return IntegerType.numbers.toMutableList()

		val output = mutableListOf<String>()

		for(i in 0..9) {
			output.add("${input[0]}$i")
			if(!input[0].contains(".")) {
				output.add("$number.")
			}
		}
		return output
	}
}