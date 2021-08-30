package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object CommandSenderType : ArgumentType(CommandSender::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return sender
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return mutableListOf(" ")
	}
}