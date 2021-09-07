package org.lupus.commands.core.data

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.help.GenericCommandHelpTopic
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.messages.I18n
import org.lupus.commands.core.utils.ReflectionUtil.getPrivateField
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class CommandLupi(
	name: String,
	description: String,
	val syntax: String,
	aliases: List<String>,
	val subCommands: MutableList<CommandLupi>,
	val method: Method?,
	val declaringClazz: Class<*>,
	val parameters: List<ArgumentType>,
	val pluginRegistering: JavaPlugin,
	val executor: ArgumentType?,
	val conditions: MutableList<ConditionFun> = mutableListOf(),
	permission: String = "",
	fullName: String = name,
	val help: Boolean = false,
	val async: Boolean = false
) : Command(name, description, syntax, aliases)
{
	init {
		this.permission = permission
		usage = "/$fullName $syntax"
	}
	var fullName: String = fullName
		private set

	val tagRgx = "<([^>]*)>".toRegex()

	override fun getDescription(): String {
		return LegacyComponentSerializer
			.legacyAmpersand()
			.serialize( MiniMessage
				.get()
				.parse(
					description.replace(tagRgx) {
						val tagName = it.groups[1]?.value ?: return@replace it.value
						I18n.getUnformatted(pluginRegistering, tagName)
					}
				)
			)
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


	private fun parseHelp(sender: CommandSender): Boolean {
		for (subCommand in subCommands) {
			val command = subCommand.fullName
			val syntax = subCommand.syntax
			val desc = subCommand.description
			sender.sendMessage(I18n[pluginRegistering, "help-syntax", "command", command, "syntax", syntax, "desc", desc])
		}
		return true
	}

	private fun runCommand(sender: CommandSender, args: Array<out String>) {
		val clazz = declaringClazz
		val obj = clazz.getDeclaredConstructor()?.newInstance()
		if (obj == null) {
			sender.sendMessage(I18n[pluginRegistering, "something-wrong"])
			return
		}
		runCommand(sender, args, obj)
	}
	private fun runCommand(sender: CommandSender, args: Array<out String>, obj: Any) {
		val subCMD = resolveSubCommand(sender, args)

		val cmdParams = getCommandParameters(sender, args)
		if (method != null && subCMD == null) {
			if (cmdParams == null) {
				sender.sendMessage(I18n[pluginRegistering, "bad-arg", "command", fullName, "syntax", syntax])
				return

			}
			for (condition in conditions) {
				val result = condition.run(sender, this, *cmdParams.toTypedArray())
				if(!result) {
					condition.getResponse(sender, this, *cmdParams.toTypedArray())
					return
				}
			}
			val res = method.invoke(obj, *cmdParams.toTypedArray())
			if(res != null)
				sendResponse(sender, res)
		}

		if (subCMD != null) {
			val parameterSize = cmdParams?.size ?: 0
			if(parameterSize-1 == args.size) {
				sender.sendMessage(I18n[pluginRegistering, "bad-arg", "command", fullName, "syntax", syntax])
				return
			}
			val clazz = subCMD.declaringClazz
			val types = getParametersTypes()
			var inst: Any = obj
			if(cmdParams == null && declaringClazz == clazz) {
				subCMD.runCommand(sender, getArgs(parameterSize+1, args), inst)
			}
			else if(cmdParams != null) {
				lateinit var constructor: Constructor<*>
				try {
					constructor = clazz.getDeclaredConstructor(*types.toTypedArray())
				}
				catch(ex: Exception) {
					ex.printStackTrace()
					sender.sendMessage(I18n[pluginRegistering, "something-wrong"])
					return
				}
				inst = constructor.newInstance(*getCMDsArgs(1,*cmdParams.toTypedArray()))
				subCMD.runCommand(sender, getArgs(parameterSize, args), inst)
			}
			else {
				// It shouldn't technically go here!
				sender.sendMessage(I18n[pluginRegistering, "something-wrong"])
			}
		}


	}

	private fun getCommandParameters(sender: CommandSender, args: Array<out String>): MutableList<Any>? {
		val arguments = mutableListOf<Any>()
		if(executor == null)
			return null
		if (!executor.isTheArgumentOfThisType(sender::class.java)) {
			sender.sendMessage(I18n[pluginRegistering, "not-for-type", "command", fullName, "syntax", syntax])
			return null
		}
		arguments.add(sender)
		for ((iteration, parameter) in parameters.withIndex()) {
			val endOffset = if (parameter.argumentSpan == -1) args.size else iteration+parameter.argumentSpan
			var value: Any? =
			try {
				parameter.conversion(sender, *getArgs(iteration, endOffset, args))
			} catch(ex: Exception) {
				null
			}
			if (value == null) {
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
		if (res is String) {
			sender.sendMessage(MiniMessage.get().parse(res))
		}
		if (res is TextComponent)
			sender.sendMessage(res)
		if (res is Array<*> && res.size >= 1) {
			if (res[0] is String) {
				val message = res[0] as String
				val placeHolderInput = res.toMutableList()
				placeHolderInput.removeFirst()
				placeHolderInput.removeIf { it == null }
				sender.sendMessage(
					MiniMessage
						.get()
						.parse(
							message,
							*placeHolderInput.toTypedArray() as Array<out Any>
						)
				)
			}
		}

	}


	fun tabComplete(sender: CommandSender, args: List<String>): MutableList<String> {
		val tabComplete = suggestSubCommand(sender, args)
		if (args.size > parameters.size || method == null)
			return tabComplete
		val paramIDX = args.size - 1
		if(paramIDX < 0)
			return tabComplete
		val parameter = parameters[paramIDX]
		val autoComplete = parameter.autoComplete(sender, *args.toTypedArray())
		tabComplete.addAll(autoComplete)

		return tabComplete
	}

	fun resolveSubCommand(sender: CommandSender, args: Array<out String>): CommandLupi? {
		val parameters = getParametersTypes()

		val parSize = parameters.size
		if (args.size < parSize+1)
			return null


		val commandArg = args[parSize].lowercase()
		if (help) {
			val commandName = I18n[pluginRegistering, "help-command-name"]
			val plainTextCMD = PlainTextComponentSerializer.plainText().serialize(commandName)
			if (commandArg == plainTextCMD) {
				parseHelp(sender)
				return null
			}
		}

		for (subCommand in subCommands) {
			if (!testPermissionSilent(sender)) {
				continue
			}
			val name = subCommand.name
			if (name.lowercase() == commandArg) {
				return subCommand
			}
		}
		return null
	}

	fun suggestSubCommand(sender: CommandSender, args: List<String>): MutableList<String> {
		val parameters = getParametersTypes()

		val parSize = parameters.size
		if (parSize >= args.size)
			return mutableListOf()

		val commandArg = args[parSize]
		val commandList = mutableListOf<String>()

		if (help) {
			val commandName = I18n[pluginRegistering, "help-command-name"]
			val plainTextCMD = PlainTextComponentSerializer.plainText().serialize(commandName)
			if (plainTextCMD.startsWith(commandArg)) {
				commandList.add(plainTextCMD)
			}
		}

		for (subCommand in subCommands) {
			val name = subCommand.name
			if (!testPermissionSilent(sender)) {
				continue
			}
			if (name.lowercase().startsWith(commandArg.lowercase())) {
				val res = suggestSubCommandParse(parSize, args, sender)
				if(res != null)
					return res
				commandList.add(name.lowercase())
			}
		}
		return commandList
	}

	private fun suggestSubCommandParse(
		parSize: Int,
		args: List<String>,
		sender: CommandSender
	): MutableList<String>? {
		if (parSize < args.size) {
			val subCommand = resolveSubCommand(sender, *args.toTypedArray()) ?: return null
			val offset = parSize+1
			return subCommand.tabComplete(sender, getArgs(offset, args.toTypedArray()).toList())
		}
		return null
	}

	var registered = false
	/**
	 * Registers the command for given plugin
	 */
	fun registerCommand(plugin: JavaPlugin) {
		if (registered)
			return

		try {
			val result: Any = getPrivateField(
				Bukkit.getServer().pluginManager,
				"commandMap"
			)

			val commandMap = result as SimpleCommandMap
			commandMap.register(super.getName(), plugin.name, this)
			val helpTopic = GenericCommandHelpTopic(this)
			Bukkit.getServer().helpMap.addTopic(helpTopic)
			registered = true
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
	fun getCMDsArgs(offset: Int, args: Array<out Any>): Array<out Any> {
		var endOffset = -1
		if (endOffset == -1)
			endOffset = args.size
		val arguments = Array<Any>(endOffset-offset) { "$it" }
		System.arraycopy(args, offset, arguments, 0, endOffset-offset)
		return arguments
	}

	override fun toString(): String {
		return "\tname:$fullName,\n\tdesc:$description,\n\tsyntax:$syntax,\n\tmethod:${method?.name},\n\tpermission:$permission"
	}
}
