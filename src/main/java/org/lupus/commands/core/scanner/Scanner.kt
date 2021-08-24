package org.lupus.commands.core.scanner

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.*
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.events.AsyncTabComplete
import org.lupus.commands.core.managers.MainCMDs
import org.lupus.commands.core.managers.RegisteredCmdClasses
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.logging.Level

class Scanner(
	private val plugin: JavaPlugin,
	private val permissionPrefix: String = ""
) {
	private var packageName: String = ""
	val commands: MutableList<CommandLupi> = mutableListOf()
	fun scan(packageName: String) {

		Bukkit.getPluginManager().registerEvents(AsyncTabComplete(), plugin)

		this.packageName = packageName
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
		outMsg("   Time elapsed = ${System.currentTimeMillis() - timing}ms")

		outMsg("[LCF] Performing scan for plugin ${clazz.simpleName}")

		timing = System.currentTimeMillis()
		val res = reflections.allTypes.filter {
			val pattern =  Regex("$packageName.*(Command|CMD)")
			it.contains(pattern)
		}

		outMsg("")
		outMsg("")
		outMsg("")
		outMsg("[LCF] Stopped scanning classes found ${res.size} applicable classes")
		outMsg("   Time elapsed = ${System.currentTimeMillis() - timing}ms")
		outMsg("[LCF] Starting to scan classes...")

		timing = System.currentTimeMillis()
		for (re in res) {
			outMsg("[LCF] Scanning $re")
			val secondClazz = clazz.classLoader.loadClass(re)
			val command = scanClass(secondClazz)
			if (command != null)
				commands.add(command)
			outMsg("[LCF] Scan ended successfully")
		}

		outMsg("")
		outMsg("")
		outMsg("[LCF] Stopped scanning classes successfully scanned all classes")
		outMsg("   Time elapsed = ${System.currentTimeMillis() - timing}ms")

		registerCommands()
		registerSubCommands()
	}

	private fun registerSubCommands() {
		for (command in commands) {
			registerSubCommand(command)
		}
	}
	private fun registerSubCommand(command: CommandLupi) {
		for (subCommand in command.subCommands) {
			val cmdPass = getCommandPass(subCommand.method) ?: continue
			subCommand.subCommands.add(cmdPass)
			cmdPass.permission = subCommand.permission + cmdPass.name
			// I hate funny dev that pointed to the class that passes command to itself :)
			// ~Lupus
			registerSubCommand(subCommand)
		}
	}

	private fun getCommandPass(method: Method?): CommandLupi? {
		if (method == null)
			return null
		val cmdPass = method.getAnnotation(CMDPass::class.java)?.commandPath ?: return null
		return RegisteredCmdClasses[cmdPass]
	}


	private fun registerCommands() {
		outMsg("[LCF] Started registering commands")
		val timing = System.currentTimeMillis()

		for (command in commands) {
			if (command.subCommand)
				continue
			MainCMDs[command.name.lowercase()] = command
			command.registerCommand(plugin)
		}
		val clazz: Class<out Server?> = Bukkit.getServer().javaClass
		val m = clazz.getDeclaredMethod("syncCommands")
		m.invoke(Bukkit.getServer())

		outMsg("[LCF] Stopped registering command successfully")
		outMsg("    Time elapsed = ${System.currentTimeMillis() - timing}ms")
	}

	private fun scanClass(clazz: Class<*>): CommandLupi? {
		val simpleName = clazz.simpleName
		val commandName = simpleName.split(Regex("Command|CMD"))[0].lowercase()
		val description = clazz.getAnnotation(Desc::class.java)?.desc ?: ""
		val aliases = clazz.getAnnotation(Aliases::class.java)?.aliases?.split("|") ?: arrayListOf()
		val async = clazz.getAnnotation(Async::class.java) != null
		val helpCMD = clazz.getAnnotation(HelpCMD::class.java) != null
		val subCommand = clazz.getAnnotation(SubCommand::class.java) != null

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
		outMsg("   Time elapsed = ${System.currentTimeMillis() - timing}ms")
		outMsg("[LCF] Starting building main command")

		val cmdBuilder = CommandBuilder(plugin, commandName, description, defaultMethod?.method, helpCMD, async, subCommand)
		if (defaultMethod != null) {
			if (defaultMethod.method != null) {
				cmdBuilder.addParameters(defaultMethod.method!!.parameters.toList())
			}
			cmdBuilder.addAlias(aliases)
		}
		cmdBuilder.addSubCommands(subCommands)

		val builtCommand = cmdBuilder.build()
		RegisteredCmdClasses[clazz.name] = builtCommand


		outMsg("[LCF] Main Command Built!")
		return builtCommand
	}
	private fun scanMethod(method: Method): CommandLupi? {
		if(method.parameterCount == 0) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to not have executor parameter at least aborting..")
			return null
		}
		val commandName = method.name
		val commandArgs = method.parameters
		var first = true
		val description = method.getAnnotation(Desc::class.java)?.desc ?: ""
		val aliases = method.getAnnotation(Aliases::class.java)?.aliases?.split("|") ?: arrayListOf()
		val async = method.getAnnotation(Async::class.java) != null
		val syntax = method.getAnnotation(Syntax::class.java)
		val permission = getPermissionForMethod(method)
		val cmdBuilder = CommandBuilder(plugin, commandName, description, method, async)

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
		val fullName = 	method
			.declaringClass
			.simpleName
			.split(Regex("Command|CMD"))[0].lowercase() + " "+ commandName.lowercase()

		cmdBuilder.setFullName(fullName)
		cmdBuilder.setPermission(permission)

		if (syntax != null)
			cmdBuilder.setSyntax(syntax.syntax)

		outMsg("[LCF] Built command $fullName")
		outMsg("[LCF] Command Permission: $permission")
		return cmdBuilder.build()
	}

	private fun getPermissionForMethod(method: Method): String {
		if(method.isAnnotationPresent(NoPerm::class.java) || method.declaringClass.isAnnotationPresent(NoPerm::class.java)) {
			return ""
		}
		var clazzPermission = method.declaringClass.getAnnotation(Perm::class.java)?.permission ?: method.declaringClass.simpleName
		val methodPermission = method.getAnnotation(Perm::class.java)?.permission ?: method.name
		if (method.isAnnotationPresent(Perm::class.java) && method.declaringClass.isAnnotationPresent(Perm::class.java)) {
			return "$clazzPermission.$methodPermission"
		}
		if (!method.declaringClass.isAnnotationPresent(Perm::class.java)) {
			clazzPermission = "${plugin.name.lowercase()}.$clazzPermission"
		}
		var packagePrefix = method.declaringClass.name.removePrefix(packageName).removeSuffix(".${method.declaringClass.simpleName}")
		// These last two checks are sanity checks if something goes wrong we dont have 2 dots near each other
		if(packagePrefix != "") {
			packagePrefix += "."
			packagePrefix = packagePrefix.lowercase()
		}
		if (clazzPermission != "") {
			clazzPermission += "."
			clazzPermission = clazzPermission.lowercase()
		}

		val permission = "$permissionPrefix$packagePrefix$clazzPermission${method.name.lowercase()}"
		return permission
	}


	private fun outMsg(string: String, level: Level) {
		Bukkit.getLogger().log(level, string)
	}
	private fun outMsg(string: String) {
		outMsg(string, Level.INFO)
	}

}