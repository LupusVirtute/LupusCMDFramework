package org.lupus.commands.core.scanner

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import io.github.classgraph.ClassGraph
import io.papermc.lib.PaperLib
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.help.GenericCommandHelpTopic
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
import org.lupus.commands.core.scanner.DefaultModifiers.fieldsModifier
import org.lupus.commands.core.scanner.DefaultModifiers.methodMods
import org.lupus.commands.core.scanner.DefaultModifiers.paramModifiers
import org.lupus.commands.core.utils.FileUtil
import org.lupus.commands.core.utils.LogUtil.outMsg
import org.lupus.commands.core.utils.ReflectionUtil
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path

class Scanner(
	private val plugin: JavaPlugin,
	private val permissionPrefix: String = "",
	private val namingSchema: Regex = Regex("Command|CMD")
) {

	private var threadsRunning: AtomicInteger = AtomicInteger(0)
	private var packageName: String = ""
	private val commands: MutableList<CommandLupi> = Collections.synchronizedList(mutableListOf())
	private val cmdPasses: MutableList<Class<*>> = mutableListOf()


	/**
	 * Scans given package path
	 */
	fun scan(packageName: String, exportResults: Boolean = false) {

		initPluginI18n(plugin)
        initPluginTabComplete(plugin)

        this.packageName = packageName
		val pluginClazz = plugin::class.java
		outMsg("[LCF] Reflections scan started")
		var timing = System.currentTimeMillis()

		val scanResult = ClassGraph()
			.enableAllInfo()
			.acceptPackages(packageName)
			.scan()

		outMsg("[LCF] Reflections scan ended")
		outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")

		outMsg("[LCF] Performing scan for plugin ${pluginClazz.simpleName}")

		timing = System.currentTimeMillis()
		val classResults = scanResult.allClasses.loadClasses().filter {
			val pattern =  Regex("$packageName.*($namingSchema)")
			it.canonicalName ?: return@filter false
			it.canonicalName.contains(pattern)
		}

		outMsg("")
		outMsg("")
		outMsg("")
		outMsg("[LCF] Stopped scanning classes found ${classResults.size} applicable classes")
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
			fieldsModifier,
			namingSchema,
			permissionPrefix
		)

		val results = mutableListOf<List<CommandLupi>>()

		for (secondClazz in classResults) {
			threadsRunning.incrementAndGet()
			object : BukkitRunnable() {
				override fun run() {
					outMsg("[LCF] Scanning ${secondClazz.simpleName}")

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
								cmdPasses.addAll(command.cmdPasses)
							}
						}
					}
					catch(ex: Exception) {
						ex.printStackTrace()
					}
					threadsRunning.decrementAndGet()
					outMsg("[LCF] Scan of $secondClazz ended successfully")
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
						exportResults(results)

					cmdPasses.forEach { cmdPass ->
						commands.removeAll { command ->
							command.declaringClazz.isAssignableFrom(cmdPass)
						}
					}

					registerBuiltCommands(plugin, commands)
					cancel()
				}
			}
		}.runTaskTimer(plugin, 1L,1L)
	}

	/**
	 * Export given results from scanner to the exportedCommands.json
	 */
	private fun exportResults(results: MutableList<List<CommandLupi>>) {
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
	}

	companion object {
		/**
		 * Serves as a check if main I18n messages were registered
		 */
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
			val cmdPasses = mutableListOf<Class<*>>()

			for (command in commands) {
				builtCommands.addAll(command.build())
				cmdPasses.addAll(command.cmdPasses)
			}

			cmdPasses.forEach { cmdPass ->
				builtCommands.removeAll { command ->
					command.declaringClazz.isAssignableFrom(cmdPass)
				}
			}

			registerBuiltCommands(plugin, builtCommands)
		}
		/**
		 * Registers commands you provide to Bukkit command map
		 */
		fun registerBuiltCommands(plugin: JavaPlugin, commands: List<CommandLupi>) {
			outMsg("[LCF] Started registering commands")
			val timing = System.currentTimeMillis()

			val commandsToRegister = mutableListOf<CommandLupi>()


			for (command in commands) {
				val lowerCaseName = command.name.lowercase()
				MainCMDs[lowerCaseName] = command
				MainCMDs["${plugin.name}:$lowerCaseName"] = command
				for (alias in command.aliases) {
					val lowerAlias = alias.lowercase()
					MainCMDs["${plugin.name}:$lowerAlias"] = command
					MainCMDs[lowerAlias] = command
				}

				commandsToRegister.add(command)
			}
			commandMapRegisterCommands(plugin, commandsToRegister)

			val clazz: Class<out Server?> = Bukkit.getServer().javaClass
			val m = clazz.getDeclaredMethod("syncCommands")
			m.invoke(Bukkit.getServer())

			outMsg("[LCF] Stopped registering commands successfully")
			outMsg("\tTime elapsed = ${System.currentTimeMillis() - timing}ms")
		}

		@Throws(NullPointerException::class)
		private fun commandMapRegisterCommands(plugin: JavaPlugin, commands: List<CommandLupi>) {
				val commandMap: SimpleCommandMap? =
					try {
						ReflectionUtil.getPrivateField(
							Bukkit.getServer().pluginManager,
							"commandMap"
						) as SimpleCommandMap
					} catch (e: Exception) {
						e.printStackTrace()
						null
					}
						?: throw NullPointerException("Somehow command map can't be accesed")

			for (command in commands) {
				commandMap?.register(command.name, plugin.name, command)
				val helpTopic = GenericCommandHelpTopic(command)
				Bukkit.getServer().helpMap.addTopic(helpTopic)
				command.registered = true
			}
		}
	}
}
