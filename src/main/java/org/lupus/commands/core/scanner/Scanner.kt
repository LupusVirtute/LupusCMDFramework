package org.lupus.commands.core.scanner

import io.papermc.lib.PaperLib
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.listeners.AsyncTabComplete
import org.lupus.commands.core.listeners.SyncTabComplete
import org.lupus.commands.core.managers.MainCMDs
import org.lupus.commands.core.messages.I18n
import org.lupus.commands.core.scanner.DefaultModifiers.anyMods
import org.lupus.commands.core.scanner.DefaultModifiers.clazzMods
import org.lupus.commands.core.scanner.DefaultModifiers.methodMods
import org.lupus.commands.core.scanner.DefaultModifiers.paramModifiers
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.util.logging.Level

class Scanner(
	private val plugin: JavaPlugin,
	private val permissionPrefix: String = "",
	private val namingSchema: Regex = Regex("Command|CMD")
) {
	companion object {
		private var reg = false
	}
	var debug: Boolean = false

	val pluginClazzLoader: ClassLoader = plugin::class.java.classLoader
	private var packageName: String = ""
	val commands: MutableList<CommandLupi> = mutableListOf()
	fun scan(packageName: String) {

		I18n.init()
		if (PaperLib.isPaper() && !reg) {
			reg = true
			Bukkit.getPluginManager().registerEvents(AsyncTabComplete(), plugin)
		} else if (!reg) {
			Bukkit.getPluginManager().registerEvents(SyncTabComplete(), plugin)
		}
		I18n.init(plugin)

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
		for (re in res) {
			outMsg("[LCF] Scanning $re")
			val secondClazz = clazz.classLoader.loadClass(re)

			if (ClazzScanner.isClazzSubCommand(secondClazz)) continue

			val command = ClazzScanner(
				secondClazz,
				plugin,
				packageName,
				clazzMods,
				anyMods,
				methodMods,
				paramModifiers,
				namingSchema,
				permissionPrefix
			)
				.scan()
			if (command != null) {
				val built = command.build()
				commands.addAll(built)
			}
			outMsg("[LCF] Scan ended successfully")
		}

		outMsg("")
		outMsg("")
		outMsg("[LCF] Stopped scanning classes successfully scanned all classes")
		outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")

		registerCommands()
	}

	private fun registerCommands() {
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

		outMsg("[LCF] Stopped registering command successfully")
		outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")
	}



	private fun outMsg(string: String, level: Level) {
		Bukkit.getLogger().log(level, string)
	}
	private fun outMsg(string: String) {
		if (!debug)
			return
		outMsg(string, Level.INFO)
	}

}