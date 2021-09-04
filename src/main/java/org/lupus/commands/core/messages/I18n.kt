package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.logging.Level
import java.util.regex.Pattern
import kotlin.collections.HashMap

object I18n : HashMap<JavaPlugin?, MutableMap<String, Properties>>() {
	private var loaded = false

	private val locales: MutableMap<JavaPlugin?, String> = hashMapOf()

	fun init() {
		if (loaded)
			return
		loaded = true
		val properties: Properties = Properties()
		val config = this::class.java.classLoader.getResourceAsStream("locales/messages-en.properties") ?: return
		properties.load(config)
		this[null] = hashMapOf()
		this[null]!!["en"] = properties
		locales[null] = "en"
	}
	fun init(plugin: JavaPlugin) {
		this[plugin] = hashMapOf()
		loadResourcesInDir("locales", plugin)
	}

	fun loadResourcesInDir(dir: String, plugin: JavaPlugin? = null) {
		val refl =
			Reflections("$dir/",ResourcesScanner())
		val resources = refl.getResources(Pattern.compile("[^.]*.properties"))
		for (resource in resources) {
			val locale = resource.split(Regex("messages-"))[1].split(".properties")[0]
			var resStream: InputStream?
			if (plugin == null)
				resStream = this::class.java.getResourceAsStream(resource)
			else
				resStream = plugin.getResource(resource)

			if (resStream == null) {

				Bukkit.getLogger().log(Level.SEVERE, "[LCF] Severe problem with identyfing resource stream for I18n")
				Bukkit.getLogger().log(Level.SEVERE, " File: $resource")
				continue
			}
			val properties = Properties()
			properties.load(InputStreamReader(resStream,"utf8"))
			this[plugin]!![locale] = properties
		}
	}
	fun setLocale(plug: JavaPlugin, locale: String) {
		locales[plug] = locale
	}

	operator fun get(plugin: JavaPlugin?, index: String, vararg objects: String): Component {
		if (this[plugin] == null) {
			return get(null, index, *objects)
		}
		val locale = locales[plugin]
		if (this[plugin]!![locale] == null) {
			return get(null, index, *objects)
		}
		if (this[plugin]!![locale] == null && plugin == null) {
			return Component.text(index)
		}
		if (this[plugin]!![locale] == null) {
			return get(plugin, index, *objects)
		}
		val mess = this[plugin]!![locale] ?: return get(null, index,*objects)

		return MiniMessage
			.get()
			.parse(
				mess.getProperty(index, index),
				*objects
			)
	}

}