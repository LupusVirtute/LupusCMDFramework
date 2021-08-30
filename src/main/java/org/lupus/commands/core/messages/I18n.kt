package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ConfigurationBuilder
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

object I18n : HashMap<JavaPlugin?, MutableMap<String, Properties>>() {
	var loaded = false
	var language: String = "en"

	private val locales: MutableMap<JavaPlugin, String> = hashMapOf()

	fun init() {
		if (loaded)
			return
		loaded = true
		val properties: Properties = Properties()
		val config = this::class.java.classLoader.getResourceAsStream("locales/messages.properties") ?: return
		properties.load(config)
		this[null] = hashMapOf()
		this[null]!!["en"] = properties
	}
	fun init(plugin: JavaPlugin) {
		if (plugin == null)
			throw IllegalAccessException("You can't access original Messages object messages")
		this[plugin] = hashMapOf()

	}

	fun getResourcesInDir(dir: String) {
		val refl =
			Reflections("$dir/",ResourcesScanner())
		val resources = refl.getResources(Pattern.compile("[^.]*.properties"))
		for (resource in resources) {
			val locale = resource.split(Regex("messages-"))[1].split(".properties")[0]
			val resStream = this::class.java.getResourceAsStream(resource)
			val properties = Properties()
			properties.load(resStream)
			this[null]!![locale] = properties
		}
	}
	fun setLocale(plug: JavaPlugin, locale: String) {
		locales[plug] = locale
	}

	operator fun get(plugin: JavaPlugin?, index: String, vararg objects: String): Component {
		if (this[plugin] == null) {
			return get(null, index, *objects)
		}
		if (this[plugin]!![language] == null) {
			return get(null, index, *objects)
		}
		if (this[plugin]!![language] == null && plugin == null) {
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