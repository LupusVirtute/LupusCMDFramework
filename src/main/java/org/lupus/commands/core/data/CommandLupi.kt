package org.lupus.commands.core.data

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.help.GenericCommandHelpTopic
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.CMDPass
import org.lupus.commands.core.annotations.Conditions
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.arguments.types.PlayerType
import org.lupus.commands.core.managers.ConditionManager
import org.lupus.commands.core.messages.I18n
import org.lupus.commands.core.utils.ReflectionUtil.getPrivateField
import org.lupus.commands.core.utils.StringUtil
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class CommandLupi(
	name: String,
	description: String,
	val syntax: String,
	aliases: List<String>,
	val subCommands: MutableList<CommandLupi>,
	val method: Method?,
	val parameters: List<ArgumentType>,
	val pluginRegistering: JavaPlugin,
	permission: String = "",
	val fullName: String = "",
	val help: Boolean = false,
	val async: Boolean = false,
	val subCommand: Boolean = false,
) : Command(name, description, syntax, aliases)
{
	val conditions: MutableList<ConditionFun> = mutableListOf()
	init {
		this.permission = permission
		if(method != null) {
			val conditions = method.getAnnotation(Conditions::class.java)
			if (conditions != null) {
				val cond = conditions.conditions.split("|")
				for (s in cond) {
					val conditionFun = ConditionManager[s.lowercase()] ?: continue
					this.conditions.add(conditionFun)
				}
			}

		}
	}

	override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		val permission = this.permission ?: ""
		if(!sender.isOp && permission != "" && !sender.hasPermission(permission)) {
			sender.sendMessage(I18n[pluginRegistering, "no-perm", "permission", permission ?: ""])
			return true
		}
		if (!async)
			runCommand(sender, args)
		else {
			Bukkit
				.getScheduler()
				.runTaskAsynchronously(pluginRegistering, Runnable {
					runCommand(sender, args)
				})
		}
		return true
	}

	private fun parseSubCommands(sender: CommandSender, args: Array<out String>): Boolean {
		if(args.isEmpty())
			return false
		var idx = 0
		if (method?.isAnnotationPresent(CMDPass::class.java) == true) {
			if (parameters.size <= args.size)
				idx = parameters.size-1
		}
		val subArgument = args[idx]
		if (help) {
			if (parseHelp(sender, subArgument)) {
				return true
			}
		}
		for (subCommand in subCommands) {
			if (subCommand.aliases.contains(subArgument) || subCommand.name == subArgument) {
				dispatchCommandToSubCommand(sender, subArgument, args, subCommand)
				return true
			}
		}
		return false
	}

	private fun parseHelp(sender: CommandSender, subArgument: String): Boolean {
		if (subArgument == "help") {
			return false
		}
		for (subCommand in subCommands) {
			val command = subCommand.fullName
			val syntax = subCommand.usage
			val desc = subCommand.description
			sender.sendMessage(I18n[pluginRegistering, "help-syntax", "command", command, "syntax", syntax, "desc", desc])
		}
		return true
	}

	private fun dispatchCommandToSubCommand(sender: CommandSender, commandLabel: String, args: Array<out String>, subCommand: CommandLupi) {
		val prefixIDX = if (parameters.size > 1) parameters.size else 1
		var argumentArray: Array<String> = Array(args.size - prefixIDX) { "$it" }
		if (prefixIDX <= args.size-1) {
			System.arraycopy(args, prefixIDX, argumentArray, 0, args.size - prefixIDX)
		}

		var commandObj: Any? = null
		val subCommandClazz = subCommand.method?.declaringClass
		if (
			method != null
			&& method.isAnnotationPresent(CMDPass::class.java)
			&& subCommandClazz != method.declaringClass
			&& parameters.size < args.size
		) {
			if (subCommandClazz == null) {
				sender.sendMessage(I18n[pluginRegistering, "something-wrong"])
				return
			}
			val parameterTypes = getParametersTypes()
			parameterTypes.removeAt(0)
			val constructor: Constructor<*>
			try {
				constructor = subCommandClazz.getConstructor(*parameterTypes.toTypedArray())
			}
			catch(ex: Exception) {
				ex.printStackTrace()
				sender.sendMessage(I18n[pluginRegistering, "something-wrong"])
				return
			}
			val parameters = getCommandParameters(sender, args) ?: return
			parameters.removeAt(0)
			commandObj = constructor.newInstance(*parameters.toTypedArray())

		}
		if (commandObj != null)
			subCommand.runCommand(sender, getArgs(parameters.size-1, args), commandObj)
		else
			subCommand.execute(sender, commandLabel, argumentArray)
	}

	private fun runCommand(sender: CommandSender, args: Array<out String>) {
		val clazz = method?.declaringClass
		val obj = clazz?.getConstructor()?.newInstance()
		runCommand(sender, args, obj)
	}
	private fun runCommand(sender: CommandSender, args: Array<out String>, obj: Any?) {
		if (conditions.isNotEmpty()) {
			for (condition in conditions) {
				val output = condition.run(sender, args)
				if (!output) {
					sender.sendMessage(condition.getResponse(sender, args))
					return
				}
			}
		}
		if (subCommands.isNotEmpty()) {
			val result = parseSubCommands(sender, args)
			if (result)
				return
		}
		if (method == null || obj == null) {
			return
		}
		// Minus one because we take into account player
		if (args.size < parameters.size-1) {
			sender.sendMessage(I18n[pluginRegistering, "bad-arg", "command", fullName, "syntax", syntax])
			return
		}
		val arguments = getCommandParameters(sender, args) ?: return

		val argArray = Array<Any>(arguments.size) { it }
		for ((i, argument) in arguments.withIndex()) {
			argArray[i] = argument
		}

		val response = method?.invoke(obj, *argArray) ?: return
		sendResponse(sender, response)
	}

	private fun getCommandParameters(sender: CommandSender, args: Array<out String>): MutableList<Any>? {
		var first = true
		var iteration = 0
		val arguments = mutableListOf<Any>()
		for (parameter in parameters) {
			if (first) {
				if (!parameter.isTheArgumentOfThisType(sender::class.java)) {
					sender.sendMessage(I18n[pluginRegistering, "not-for-type", "command", fullName, "syntax", syntax])
					return null
				}
				first = false
				arguments.add(sender)
				continue
			}
			val endOffset = if (parameter.argumentSpan == -1) args.size else iteration+parameter.argumentSpan
			val value = parameter.conversion(sender, *getArgs(iteration, endOffset, args))
			iteration++
			if (value == null) {
				sender.sendMessage(I18n[pluginRegistering, "bad-arg", "command", fullName, "syntax", syntax])
				return null
			}
			arguments.add(
				value
			)
		}
		return arguments
	}
	private fun getParametersTypes(): MutableList<out Class<*>> {
		val args = mutableListOf<Class<*>>()
		for (parameter in parameters) {
			args.add(parameter.clazz)
		}
		return args
	}

	private fun sendResponse(sender: CommandSender, res: Any) {
		if (res is String)
			sender.sendMessage(MiniMessage.get().parse(res))
		if (res is TextComponent)
			sender.sendMessage(res)
		if (res is Array<*> && res.size >= 1)
			if (res[0] is String)
				sender.sendMessage(res as Array<out String>)

	}


	fun tabComplete(sender: CommandSender, args: List<String>): MutableList<String> {
		val tabComplete = mutableListOf<String>()

		if(subCommands.isNotEmpty()) {
			if (parameters.size == args.size-1) {
				val subList = mutableListOf<String>()
				for (subCommand in subCommands) {
					subList.add(subCommand.name)
					subList.addAll(subCommand.aliases)
				}

				if(help)
					subList.add("help")

				when {
					method == null -> return subList
					method.isAnnotationPresent(CMDPass::class.java) -> return subList
					else -> tabComplete.addAll(subList)
				}
			}
			else if (parameters.size < args.size){
				for (subCommand in subCommands) {
					var prefixIDX = parameters.size
					// We need to include atleast sub command argument span so we need minimal 1
					// When we pass it to lower parts of tab execution
					prefixIDX = if (prefixIDX <= 0) 1 else prefixIDX
					val arg = args[0]
					if (
						subCommand.name.startsWith(arg)
						|| StringUtil.listContainsStringStartingWith(subCommand.aliases, arg)
					) {
						return subCommand.tabComplete(sender, getArgs(prefixIDX, args.toTypedArray()).toList())
					}
				}
			}

		}
		// Minus one because player is included in parameters
		if(parameters.size-1 < args.size) {
			tabComplete.addAll(PlayerType.autoComplete(sender, *args.toTypedArray()))
			return tabComplete
		}
		if (parameters.isEmpty()) {
			return tabComplete
		}
		var lastIDX = args.size
		val parameter = parameters[lastIDX]
		// Last argument index
		val argsLastIDX = args.size-1
		val lastOne = args[argsLastIDX]
		tabComplete.addAll(
			parameter.autoComplete(sender, lastOne)
		)
		return tabComplete
	}

	/**
	 * Registers the command for given plugin
	 */
	fun registerCommand(plugin: JavaPlugin) {
		try {
			val result: Any = getPrivateField(
				Bukkit.getServer().pluginManager,
				"commandMap"
			)

			val commandMap = result as SimpleCommandMap
			commandMap.register(super.getName(), plugin.name, this)
			val helpTopic = GenericCommandHelpTopic(this)
			Bukkit.getServer().helpMap.addTopic(helpTopic)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun getArgs(offset: Int, args: Array<out String>): Array<out String> {
		return getArgs(offset,-1, args)
	}

	fun getArgs(offset: Int, end: Int, args: Array<out String>): Array<out String> {
		var endOffset = end
		if (endOffset == -1)
			endOffset = args.size
		val arguments = Array(endOffset-offset) { "$it" }
		System.arraycopy(args, offset, arguments, 0, endOffset-offset)
		return arguments
	}
}
