package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object WorldType : ArgumentType(World::class.java, canBeWildCard = true) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getWorld(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val worlds = Bukkit
			.getWorlds()
			.map {
				it.name
			}
			.toMutableList()
		return worlds
	}

	override fun wildcard(sender: CommandSender, vararg input: String): MutableList<Any?> {
		return Bukkit.getWorlds().toMutableList()
	}
}
