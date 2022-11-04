package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.arguments.ArgumentType

object PluginType : ArgumentType(JavaPlugin::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getPluginManager().getPlugin(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val first = input.firstOrNull() ?: return mutableListOf()
		return Bukkit
			.getPluginManager()
			.plugins
			.map { it.name }
			.filter { it.startsWith(first) }
			.toMutableList()
	}
}
