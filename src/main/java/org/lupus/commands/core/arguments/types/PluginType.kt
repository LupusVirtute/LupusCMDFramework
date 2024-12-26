package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.arguments.ArgumentType

object PluginType : ArgumentType(JavaPlugin::class.java, canBeWildCard = true) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getPluginManager().getPlugin(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val first = input.firstOrNull() ?: return mutableListOf()
		val plugins = Bukkit
			.getPluginManager()
			.plugins
			.map { it.name }
			.toMutableList()
		return plugins
			.filter { it.startsWith(first) }
			.toMutableList()
	}

	override fun wildcard(sender: CommandSender, vararg input: String): MutableList<Any?> {
		return Bukkit.getPluginManager().plugins.toMutableList()
	}
}
