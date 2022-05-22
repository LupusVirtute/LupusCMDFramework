package org.lupus.commands.core.scanner

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import io.papermc.lib.PaperLib
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.listeners.AsyncTabComplete
import org.lupus.commands.core.listeners.SyncTabComplete
import org.lupus.commands.core.managers.MainCMDs
import org.lupus.commands.core.messages.I18n
import org.lupus.commands.core.scanner.DefaultModifiers.anyMods
import org.lupus.commands.core.scanner.DefaultModifiers.clazzMods
import org.lupus.commands.core.scanner.DefaultModifiers.methodMods
import org.lupus.commands.core.scanner.DefaultModifiers.paramModifiers
import org.lupus.commands.core.utils.FileUtil
import org.lupus.commands.core.utils.LogUtil.outMsg
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path

class Scanner(
	private val plugin: JavaPlugin,
	private val permissionPrefix: String = "",
	private val namingSchema: Regex = Regex("Command|CMD")
) {

	private var threadsRunning: AtomicInteger = AtomicInteger(0)
	val pluginClazzLoader: ClassLoader = plugin::class.java.classLoader
	private var packageName: String = ""
	val commands: MutableList<CommandLupi> = Collections.synchronizedList(mutableListOf())


	/**
	 * Scans given package path
	 */
	fun scan(packageName: String, exportResults: Boolean = false) {

		initPluginI18n(plugin);
		initPluginTabComplete(plugin);

		this.packageName = packageName
		val clazz = plugin::class.java
		outMsg("[LCF] Reflections scan started")
		var timing = System.currentTimeMillis()

		val reflections = Reflections(
			ConfigurationBuilder()
				.setScanners(
					SubTypesScanner(false), TypeAnnotationsScanner()
				)
				.setUrls(
					ClasspathHelper.forClassLoader(
						pluginClazzLoader
					)
				)
		)

		outMsg("[LCF] Reflections scan ended")
		outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")

		outMsg("[LCF] Performing scan for plugin ${clazz.simpleName}")

		timing = System.currentTimeMillis()
		val res = reflections.allTypes.filter {
			val pattern =  Regex("$packageName.*($namingSchema)")
			it.contains(pattern)
		}

		outMsg("")
		outMsg("")
		outMsg("")
		outMsg("[LCF] Stopped scanning classes found ${res.size} applicable classes")
		outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")
		outMsg("[LCF] Starting to scan classes...")

		timing = System.currentTimeMillis()
		val scanner = ClazzScanner(
			plugin,
			packageName,
			clazzMods,
			anyMods,
			methodMods,
			paramModifiers,
			namingSchema,
			permissionPrefix
		)

		val results = mutableListOf<List<CommandLupi>>()

		for (re in res) {
			threadsRunning.incrementAndGet()
			object : BukkitRunnable() {
				override fun run() {
					outMsg("[LCF] Scanning $re")
					val secondClazz = clazz.classLoader.loadClass(re)

					if (ClazzScanner.isClazzSubCommand(secondClazz)) {
						threadsRunning.decrementAndGet()
						return
					}
					try {
						val command = scanner.scan(secondClazz)
						if (command != null) {
							val built = command.build()

							if(exportResults)
								results.add(built)

							synchronized(commands) {
								commands.addAll(built)
							}
						}
					}
					catch(ex: Exception) {
						ex.printStackTrace()
					}
					threadsRunning.decrementAndGet()
					outMsg("[LCF] Scan of $re ended successfully")
				}

			}.runTaskAsynchronously(plugin)
		}

		outMsg("")
		outMsg("")
		outMsg("[LCF] Stopped scanning classes successfully scanned all classes")
		outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")
		object: BukkitRunnable() {
			override fun run() {
				if(threadsRunning.get() == 0) {
					if(exportResults)
						results
							.flatten()
							.map {
								it.toGsonTree()
							}
							.run {
								val json = JsonArray(this.size)
								this.forEach { json.add(it)}

								FileUtil.dumpToFile(
									GsonBuilder()
									.setPrettyPrinting()
										.create()
										.toJson(json),
									Path("./exportedCommands.json").toFile()
								)
							}

					registerBuiltCommands(plugin, commands)
					cancel()
				}
			}
		}.runTaskTimer(plugin, 1L,1L)
	}
	private fun flattenSubCommands(commandLupi: CommandLupi): List<CommandLupi> {
		val flattenedCommands = commandLupi.subCommands.map {
			flattenSubCommands(it)
		}.flatten().toMutableList()

		flattenedCommands.add(commandLupi)
		return flattenedCommands
	}

	companion object {
		private var regMain = false
		// This is cache for registered tab completers
		private var reg: HashMap<JavaPlugin, Boolean> = hashMapOf()

		/**
		 * It serves an option to initialize plugin providing I18n support and Tab Completion
		 */
		fun initPluginI18n(plugin: JavaPlugin) {
			if (!regMain) {
				regMain = true
				I18n.init()
			}
			I18n.init(plugin)
		}

		/**
		 * If you really don't want to tab complete for your plugin leave this function alone <br/>
		 * This function enables for every command registered with framework a chance to work
		 */
		fun initPluginTabComplete(plugin: JavaPlugin) {
			outMsg("[LCF] Initializing plugin tab complete")
			if (reg[plugin] == null)
				reg[plugin] = false
			if (PaperLib.isPaper() && !reg[plugin]!!) {
				reg[plugin] = true
				Bukkit.getPluginManager().registerEvents(AsyncTabComplete(), plugin)
			} else if (!reg[plugin]!!) {
				reg[plugin] = true
				Bukkit.getPluginManager().registerEvents(SyncTabComplete(), plugin)
			}
		}

		/**
		 * Small util to help with unbuilt commands from ClazzScanner or MethodScanner
		 */
		fun registerCommands(plugin: JavaPlugin, commands: List<CommandBuilder>) {
			val builtCommands = mutableListOf<CommandLupi>()
			for (command in commands) {
				builtCommands.addAll(command.build())
			}
			registerBuiltCommands(plugin, builtCommands)
		}
		/**
		 * Registers commands you provide to Bukkit command map
		 */
		fun registerBuiltCommands(plugin: JavaPlugin, commands: List<CommandLupi>) {
			outMsg("[LCF] Started registering commands")
			val timing = System.currentTimeMillis()
			for (command in commands) {
				val lowerCaseName = command.name.lowercase()
				MainCMDs[lowerCaseName] = command
				MainCMDs["${plugin.name}:$lowerCaseName"] = command
				for (alias in command.aliases) {
					val lowerAlias = alias.lowercase()
					MainCMDs["${plugin.name}:$lowerAlias"] = command
					MainCMDs[lowerAlias] = command
				}

				command.registerCommand(plugin)
			}
			val clazz: Class<out Server?> = Bukkit.getServer().javaClass
			val m = clazz.getDeclaredMethod("syncCommands")
			m.invoke(Bukkit.getServer())

			outMsg("[LCF] Stopped registering commands successfully")
			outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")
		}
	}
}