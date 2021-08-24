package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.data.CommandLupi
import java.util.*
import kotlin.collections.HashMap

object I18n : HashMap<JavaPlugin?, MutableMap<String, Properties>>() {
	init {
		// Default bindings
		this[null] = hashMapOf()
		loadConfigs()
	}

	var language: String = "en"

	private fun loadConfigs() {
		val properties: Properties = Properties()
		val config = this::class.java.classLoader.getResourceAsStream("messages.properties") ?: return
		properties.load(config)
	}
	fun init(plugin: JavaPlugin) {
		if (plugin == null)
			throw IllegalAccessException("You can't access original Messages object messages")
		this[plugin] = hashMapOf()
		Component.text().decorate()
	}

	fun init(plugin: JavaPlugin, map: Map<String, Properties>) {
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
		if (this[plugin]!![language] == null) {
			return get(plugin, index, *objects)
		}
		val mess = this[plugin]!![language] ?: return get(null, index,*objects)

		return MiniMessage
			.get()
			.parse(
				mess.getProperty(index, index),
				*objects
			)
	}
}