package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
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
		val properties = Properties()
		val config = this::class.java.classLoader.getResourceAsStream("locales/messages-en.properties") ?: return
		properties.load(config)
		this[null] = hashMapOf()
		this[null]!!["en"] = properties
		locales[null] = "en"
	}
	fun init(plugin: JavaPlugin) {
		if(this[plugin] != null)
			throw Exception("Plugin already initialized")

		this[plugin] = hashMapOf()
		loadResourcesInDir("locales", plugin)
	}
	fun resetPlugin(plugin: JavaPlugin) {
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
			addStream(plugin, resStream, resource, locale)
		}
		if (plugin == null)
			return
		val folder = File(plugin.dataFolder,dir)
		if (!folder.exists() || !folder.isDirectory) {
			return
		}
		val fileList = folder.listFiles() ?: return

		for (listFile in fileList) {
			if (listFile.isDirectory) {
				continue
			}
			val locale = listFile.name.split(Regex("messages-"))[1].split(".properties")[0]
			val stream = listFile.inputStream()
			addStream(plugin, stream, listFile.name, locale)
		}
	}
	fun addStream(plugin: JavaPlugin?, resStream: InputStream?, file: String, locale: String) {
		if (resStream == null) {
			Bukkit.getLogger().log(Level.SEVERE, "[LCF] Severe problem with identyfing resource stream for I18n")
			Bukkit.getLogger().log(Level.SEVERE, " File: $file")
			return
		}
		val properties = Properties()
		properties.load(InputStreamReader(resStream,"utf8"))
		this[plugin]!![locale] = properties
	}
	fun setLocale(plug: JavaPlugin, locale: String) {
		locales[plug] = locale
	}
	private fun getTagResolver(objects: Array<out String>): TagResolver {
		var tagResolver = TagResolver.builder()
		var iter = 0
		var lastKey = ""
		for (obj in objects) {
			if(iter % 2 == 0) {
				lastKey = obj
			}
			else {
				val placeHolder = Placeholder.parsed(lastKey, obj)
				tagResolver = tagResolver.resolver(placeHolder)
			}

			iter++
		}
		return tagResolver.build()
	}

	operator fun get(plugin: JavaPlugin?, index: String, vararg objects: String): Component {
		val mess = getUnformatted(plugin, index, *objects)


		return MiniMessage
			.miniMessage()
			.deserialize(
				mess,
				getTagResolver(objects)
			)
	}
	fun getUnformatted(plugin: JavaPlugin?, index: String, vararg objects: String): String {
		if (this[plugin] == null) {
			return getUnformatted(null, index, *objects)
		}
		val locale = locales[plugin]
		if (this[plugin]!![locale] == null) {
			return getUnformatted(null, index, *objects)
		}
		if (this[plugin]!![locale] == null && plugin == null) {
			return index
		}
		if (this[plugin]!![locale] == null) {
			return getUnformatted(plugin, index, *objects)
		}
		if (this[plugin]!![locale]!![index] == null && plugin == null) {
			return index
		}
		if (this[plugin]!![locale]!![index] == null) {
			return getUnformatted(null, index, *objects)
		}
		val output = this[plugin]!![locale]?.getProperty(index, index) ?: getUnformatted(null, index,*objects)
		return output
	}
	fun parseInput(plugin: JavaPlugin?, input: String, vararg objects: String): String {
		return LegacyComponentSerializer
			.legacySection()
			.serialize(
				MiniMessage
					.miniMessage()
					.deserialize(
						getUnformatted(plugin, input),
						getTagResolver(objects)
					)
			)
	}

}