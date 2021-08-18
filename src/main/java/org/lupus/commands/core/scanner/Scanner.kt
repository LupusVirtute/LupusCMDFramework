package org.lupus.commands.core.scanner

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.Aliases
import org.lupus.commands.core.annotations.Default
import org.lupus.commands.core.annotations.Desc
import org.lupus.commands.core.annotations.NotCMD
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.CommandLupi
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.logging.Level

class Scanner(private val plugin: JavaPlugin) {
	fun scan(packageName: String?) {
		val clazz = plugin::class.java

		outMsg("[LCF] Reflections scan started")
		var timing = System.currentTimeMillis()

		val reflections = Reflections(
			ConfigurationBuilder()
				.setScanners(
					SubTypesScanner(false), ResourcesScanner(), TypeAnnotationsScanner()
				)
				.setUrls(
					ClasspathHelper.forClassLoader(
						clazz.classLoader
					)
				)
		)

		outMsg("[LCF] Reflections scan ended")
		outMsg("   Time Taken = ${System.currentTimeMillis() - timing}")

		outMsg("[LCF] Performing scan for plugin ${clazz.simpleName}")
		timing = System.currentTimeMillis()
		val res = reflections.allTypes.filter {
			val pattern =  Regex("$packageName.*(Command|CMD)")
			it.contains(pattern)
		}
		println()
		println()
		println()
		outMsg("[LCF] Stopped scanning classes found ${res.size} applicable classes")
		outMsg("   Time taken = ${System.currentTimeMillis() - timing}")
		outMsg("[LCF] Starting to scan classes...")
		val commands = mutableListOf<CommandLupi>()
		timing = System.currentTimeMillis()
		for (re in res) {
			outMsg("[LCF] Scanning $re")
			val secondClazz = clazz.classLoader.loadClass(re)
			val command = scanClass(secondClazz)
			if (command != null)
				commands.add(command)
			outMsg("[LCF] Scan ended successfully")
		}
		println()
		println()
		outMsg("[LCF] Stopped scanning classes successfully scanned all classes")
		outMsg("   Time taken = ${System.currentTimeMillis() - timing}")
		registerCommands(commands)

	}
	private fun registerCommands(commands: List<CommandLupi>) {
		println("[LCF] Started registering commands")
		val timing = System.currentTimeMillis()

		for (command in commands) {
			command.registerCommand(plugin)
		}
		val clazz: Class<out Server?> = Bukkit.getServer().javaClass
		val m = clazz.getDeclaredMethod("syncCommands")
		m.invoke(Bukkit.getServer())

		println("[LCF] Stopped registering command successfully")
		println("    Time taken = ${System.currentTimeMillis() - timing}")
	}

	private fun scanClass(clazz: Class<*>): CommandLupi? {
		val simpleName = clazz.simpleName
		val commandName = simpleName.split(Regex("Command|CMD"))[0]
		val description = clazz.getAnnotation(Desc::class.java)?.desc ?: ""
		val aliases = clazz.getAnnotation(Aliases::class.java)?.aliases?.split("|") ?: arrayListOf()

		outMsg("[LCF] Found sup command name = $commandName")

		if (commandName == "") {
			outMsg("[LCF] Aborting command registration due to invalid naming schema")
			return null
		}

		val timing = System.currentTimeMillis()
		outMsg("[LCF] Started scanning commands inside /$commandName")
		var defaultMethod: CommandLupi? = null
		val subCommands = mutableListOf<CommandLupi>()
		for (declaredMethod in clazz.declaredMethods) {
			outMsg("[LCF] Scanning declared Method ${declaredMethod.name}")

			if (declaredMethod.isAnnotationPresent(NotCMD::class.java)) {
				outMsg("[LCF] Method ${declaredMethod.name} isn't command aborting...")
				continue
			}
			val command = scanMethod(declaredMethod)
			if (command != null) {
				if (declaredMethod.getAnnotation(Default::class.java) != null) {
					defaultMethod = command
				}
				subCommands.add(command)
			}

			outMsg("[LCF] Successfully scanned method ${declaredMethod.name}")
		}

		outMsg("[LCF] Stopped scanning commands successfully scanned all commands")
		outMsg("   Time taken = ${System.currentTimeMillis() - timing}")
		outMsg("[LCF] Starting building main command")

		val cmdBuilder = CommandBuilder(commandName, description, defaultMethod?.method)
		if (defaultMethod != null) {
			if (defaultMethod.method != null) {
				cmdBuilder.addParameters(defaultMethod.method!!.parameters.toList())
			}
			cmdBuilder.addAlias(aliases)

		}
		cmdBuilder.addSubCommands(subCommands)
		val builtCommand = cmdBuilder.build()

		outMsg("[LCF] Main Command Built!")
		return builtCommand
	}
	private fun scanMethod(method: Method): CommandLupi? {

		val commandName = method.name
		val commandArgs = method.parameters
		var first = true
		val description = method.getAnnotation(Desc::class.java)?.desc ?: ""
		val aliases = method.getAnnotation(Aliases::class.java)?.aliases?.split("|") ?: arrayListOf()

		val cmdBuilder = CommandBuilder(commandName, description, method)

		cmdBuilder.addAlias(aliases)

		for (commandArg in commandArgs) {
			if (!ArgumentTypeList.contains(commandArg.type)) {
				outMsg("[LCF] ERROR: Command argument isn't defined in ArgumentTypeList did you load your command arguments before scanning class?", Level.SEVERE)
				outMsg("If not use @NotCMD", Level.SEVERE)
				return null
			}
			if (first) {
				first = !first
				if (!CommandSender::class.java.isAssignableFrom(commandArg.type)) {
					outMsg("[LCF] First argument of method ${method.name} is not Bukkit CommandSender aborting")
					return null
				}
			}
			cmdBuilder.addParameter(commandArg)
		}

		return cmdBuilder.build()
	}
	private fun outMsg(string: String, level: Level) {
		Bukkit.getLogger().log(level, string)
	}
	private fun outMsg(string: String) {
		outMsg(string, Level.INFO)
	}

}