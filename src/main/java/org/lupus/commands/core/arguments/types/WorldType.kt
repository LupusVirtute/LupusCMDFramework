package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object WorldType : ArgumentType(WorldType::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getWorld(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		return Bukkit
			.getWorlds()
			.map {
				it.name
			}
			.toMutableList()
	}
}