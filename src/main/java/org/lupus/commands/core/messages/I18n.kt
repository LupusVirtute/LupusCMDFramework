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

	// This variable keeps in mind if the initialization of default values was already done
	private var loaded = false


	/**
	 * This is a map used to determine currently used localisation for the plugin
	 */
	private val currentlyUsedLocales: MutableMap<JavaPlugin?, String> = hashMapOf()

	/**
	 * Init default values for the I18n
	 */
	fun init() {
		if (loaded)
			return

		loaded = true

		val properties = Properties()
		val config = this::class.java.classLoader.getResourceAsStream("locales/messages-en.properties") ?: return
		properties.load(config)

		this[null] = hashMapOf()
		this[null]!!["en"] = properties

		currentlyUsedLocales[null] = "en"
	}

	/**
	 * Initialize the I18n for the given plugin
	 * @param plugin Plugin to initialize
	 */
	fun init(plugin: JavaPlugin) {
		if(this[plugin] != null)
			throw Exception("Plugin already initialized")

		checkPlugin(plugin)

		this[plugin] = hashMapOf()
		loadResourcesInDir("locales", plugin)
	}

	/**
	 * Resets I18n initialization process for given plugin <br />
	 * This basically resets the I18n for the plugin
	 * @param plugin Plugin which needs to be reset
	 */
	fun resetPlugin(plugin: JavaPlugin) {
		checkPlugin(plugin)
		loadResourcesInDir("locales", plugin)
	}

	/**
	 * Loads translations for different localisations <br />
	 * Based on the directory given <br />
	 * @param dir Directory that will be searched it can happen in the plugin jar or in the plugin directory folder depends on the check if the plugin is null <br />
	 * @param plugin Plugin that translations will be loaded for
	 */
	fun loadResourcesInDir(dir: String, plugin: JavaPlugin? = null) {
		val refl =
			Reflections("$dir/",ResourcesScanner())

		val resources = refl.getResources(Pattern.compile("[^.]*.properties"))

		for (resource in resources) {
			val locale = resource.split(Regex("messages-"))[1].split(".properties")[0]

			val resStream: InputStream? = if (plugin == null)
				this::class.java.getResourceAsStream(resource)
			else
				plugin.getResource(resource)

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

	/**
	 * Loads given properties file to be initialized for translations \
	 * <b>Only recommended to use if you have your own way of assigning input stream otherwise use <i>loadResourcesInDir()</i></b>
	 * @param plugin Plugin that gets the input stream
	 * @param resStream Input stream for properties to load from
	 * @param locale locale key for example "en"
	 * @param file file to display error message
	 */
	fun addStream(plugin: JavaPlugin?, resStream: InputStream?, locale: String,  file: String) {
		if (resStream == null) {
			Bukkit.getLogger().log(Level.SEVERE, "[LCF] Severe problem with identifying resource stream for I18n")
			Bukkit.getLogger().log(Level.SEVERE, " File: $file")
			return
		}
		val properties = Properties()
		properties.load(InputStreamReader(resStream,"utf8"))
		this[plugin]!![locale] = properties
	}

	/**
	 * Sets locale for the plugin
	 * @param plug Plugin to set locale for
	 * @param locale Localisation tag to be set for example <b>en</b>
	 */
	fun setLocale(plug: JavaPlugin, locale: String) {
		currentlyUsedLocales[plug] = locale
	}

	/**
	 * Tag resolver bundles two strings into placeholder text array needs to be atleast length of 2 to add placeholder
	 * text that is being going to change
	 * @param objects String array
	 */
	@Deprecated("Deprecated due to kyori changing the way some things are now made in components")
	fun getTagResolver(objects: Array<out String>): TagResolver {
		val tagResolver = TagResolver.builder()
		var lastKey = ""

		for ((iter, obj) in objects.withIndex()) {
			if(iter % 2 == 0) {
				lastKey = obj
			}
			else {
				val placeHolder = Placeholder.parsed(lastKey, obj)
				tagResolver.resolver(placeHolder)
			}

		}
		return tagResolver.build()
	}

	/**
	 * Tag resolver bundles two strings into placeholder text array needs to be atleast length of 2 to add placeholder
	 * text that is being going to change
	 * @param objects key value binders
	 */
	fun getTagResolver(objects: Array<out KeyValueBinder>): TagResolver {
		val tagResolverBuilder = TagResolver.builder()
		for (obj in objects) {
			val placeHolder = Placeholder.parsed(obj.key, obj.value)
			tagResolverBuilder.resolver(placeHolder)
		}
		return tagResolverBuilder.build()
	}

	/**
	 * Tag resolver bundles two strings into placeholder text array needs to be atleast length of 2 to add placeholder
	 * text that is being going to change
	 * @param placeHolders placeholders that are going to get added
	 */
	fun getTagResolver(placeHolders: Array<out TagResolver.Single>): TagResolver {
		val tagResolverBuilder = TagResolver.builder()
		for (placeHolder in placeHolders) {
			tagResolverBuilder.resolver(placeHolder)
		}
		return tagResolverBuilder.build()
	}

	/**
	 * Get MiniMessage component for given i18n
	 * @param plugin plugin that take information from
	 * @param index i18n key tag that needs to be taken
	 * @param objects every object with (index % 2 == 0) is placeholder value the other one is placeholder key
	 * @return text component for given index
	 */
	@Deprecated("Change to tag resolver instead of array of string")
	operator fun get(plugin: JavaPlugin?, index: String, vararg objects: String): Component {
		val mess = getUnformatted(plugin, index)

		return MiniMessage
			.miniMessage()
			.deserialize(
				mess,
				getTagResolver(objects)
			)
	}

	/**
	 * Get mini message component for given i18n
	 * @param plugin plugin that take information from
	 * @param index i18n key tag that needs to be taken
	 * @param tagResolver tag resolver that is going to resolve replacement tags
	 * @return text component for given index
	 */
	operator fun get(plugin: JavaPlugin?, index: String, tagResolver: TagResolver): Component {
		val mess = getUnformatted(
			plugin,
			index
		)

		return MiniMessage
			.miniMessage()
			.deserialize(
				mess,
				tagResolver
			)
	}

	/**
	 * Gets unformatted version of the translation
	 * @param plugin plugin locale that translations will be taken from if null it will use default config
	 * @param index key tag for the translation
	 */
	fun getUnformatted(plugin: JavaPlugin?, index: String): String {
		// Check if the plugin is null if yes get the default message
		if (this[plugin] == null) {
			return getUnformatted(null, index)
		}

		val locale = currentlyUsedLocales[plugin] ?: "en"
		// Checks if the locale exists for given plugin if not try to get original message
		if (this[plugin]!![locale] == null && plugin != null) {
			return getUnformatted(null, index)
		}
		// Check if the default config has any locale
		if (this[plugin]!![locale] == null && plugin == null) {
			return index
		}

		// Check if there is anything in default config
		if (this[plugin]!![locale]!![index] == null && plugin == null) {
			return index
		}

		// Check if there is value for given index
		// if not get from default config
		if (this[plugin]!![locale]!![index] == null) {
			return getUnformatted(null, index)
		}

		// Get either value from properties or default plugin
		return this[plugin]!![locale]?.getProperty(index, index)
			?: getUnformatted(null, index)
	}


	/**
	 * # Deprecated
	 * This function now just only redirects to the <b>getUnformatted(plugin, index)</b>
	 * for compatibility sake
	 */
	@Deprecated("Old getUnformatted method that needs objects but it really doesn't need them probably forgot to remove them",
		ReplaceWith("getUnformatted(plugin, index)", "org.lupus.commands.core.messages.I18n.getUnformatted")
	)
	fun getUnformatted(plugin: JavaPlugin?, index: String, vararg objects: String): String {
		return getUnformatted(plugin, index)
	}

	/**
	 * # Deprecated
	 * Given the input get the legacy formatted text for the I18n <br />
	 * Using kyori adventure  it serializes unformatted I18n message into formatted legacy one <br />
	 * <a href="https://docs.adventure.kyori.net/serializer/legacy.html">More info on kyori legacy serializer here</a>
	 * @param plugin plugin to parse input from
	 * @param key key to get I18n deserialized value from
	 */
	@Deprecated("Parse input for the placeholder array",
		ReplaceWith(
			"parseInput(plugin, input, getTagResolver(objects))",
			"org.lupus.commands.core.messages.I18n.parseInput",
			"org.lupus.commands.core.messages.I18n.getTagResolver"
		)
	)
	fun parseInput(plugin: JavaPlugin?, key: String, vararg objects: String): String {
		return LegacyComponentSerializer
			.legacySection()
			.serialize(
				this[plugin, key, getTagResolver(objects)]
			)
	}
	/**
	 * Given the input get the legacy formatted text for the I18n <br />
	 * Using kyori adventure it serializes unformatted I18n message into formatted legacy one <br />
	 * <a href="https://docs.adventure.kyori.net/serializer/legacy.html">More info on kyori legacy serializer here</a>
	 * @param plugin plugin to parse input from
	 * @param key key to get I18n deserialized value from
	 * @return serialized
	 */
	fun parseInput(plugin: JavaPlugin?, key: String, tagResolver: TagResolver): String {
		return LegacyComponentSerializer
			.legacySection()
			.serialize(
				this[plugin, key, tagResolver]
			)
	}

	/**
	 * Checks if the plugin is enabled <br />
	 * if not deny service to the use of functions from <br />
	 * I18n because I18n needs to know which plugin is using which <br />
	 * Locale and other things
	 * @throws Exception When plugin isn't enabled
	 */
	private fun checkPlugin(plugin: JavaPlugin) {
		if(!plugin.isEnabled) {
			this.remove(plugin)
			this.currentlyUsedLocales.remove(plugin)
			throw Exception("Plugin isn't enabled")
		}
	}

}