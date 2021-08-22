package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.data.CommandLupi

object Messages : HashMap<JavaPlugin?, MutableMap<String, String>>() {
	init {
		// Default bindings
		this[null] = hashMapOf()
		this[null]!!["bad-arg"] = "<red>Usage: /<command> <syntax>"
		this[null]!!["not-for-type"] = "<red>You aren't allowed to use this command"
		this[null]!!["bad-permission"] = "<red>You have no permission for this command"
		this[null]!!["something-wrong"] = "<red>Something wrong has happened when trying to execute command contact the administrator"
	}
	fun init(plugin: JavaPlugin) {
		if (plugin == null)
			throw IllegalAccessException("You can't access original Messages object messages")
		this[plugin] = hashMapOf()
		Component.text().decorate()
	}
	fun init(plugin: JavaPlugin, map: Map<String, String>) {
		init(plugin)
		for (keyPair in map) {
			this[plugin]?.put(keyPair.key, keyPair.value)
		}
	}
	operator fun get(plugin: JavaPlugin?, index: String, vararg objects: String): Component {
		if (this[plugin] == null) {
			return get(null, index, *objects)
		}
		if (this[plugin]!![index] == null && plugin == null) {
			return Component.text(index)
		}
		val mess = this[plugin]!![index] ?: return get(null, index, *objects)
		return MiniMessage.get().parse(mess, *objects)
	}
}