package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType
import java.util.*

object IntegerType : ArgumentType(Int::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return input[0].toIntOrNull()
	}

	val numbers = mutableListOf("0","1","2","3","4","5","6","7","8","9")
	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
												// This is to copy not to cast it to mutable list xD
		val number = input[0].toIntOrNull() ?: return numbers.toMutableList()

		val output = mutableListOf<String>()
		for(i in 0..9) {
			output.add("$number$i")
		}
		return output
	}
}