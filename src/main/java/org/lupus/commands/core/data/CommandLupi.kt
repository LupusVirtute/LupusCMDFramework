package org.lupus.commands.core.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.Dependency
import org.lupus.commands.core.annotations.NamedDependency
import org.lupus.commands.core.annotations.method.Default
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.components.command.prerun.PreRunConditionCheck
import org.lupus.commands.core.components.command.prerun.PreRunGetCommandParams
import org.lupus.commands.core.components.command.prerun.PreRunPermissionCheck
import org.lupus.commands.core.components.command.prerun.PreRunPickSubCommand
import org.lupus.commands.core.components.command.response.ArrayResponseComponent
import org.lupus.commands.core.components.command.response.I18nResponseComponent
import org.lupus.commands.core.components.command.response.StringResponseComponent
import org.lupus.commands.core.components.command.response.TextCompResponse
import org.lupus.commands.core.messages.I18nMessage
import org.lupus.commands.core.messages.KeyValueBinder
import org.lupus.commands.core.utils.CommandUtil
import org.lupus.commands.core.utils.StringUtil
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class CommandLupi(
	name: String,
	description: String,
	private val _syntax: String,
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
	val flags: Set<CommandFlag>,
	val optionals: HashMap<Int, Array<String>>,
	val filters: MutableList<FilterFun>,
	val injectableDependencies: HashMap<Class<*>, Any>,
	val namedInjectableDependencies: HashMap<String, Any>

) : Command(name, description, _syntax, aliases)
{
	init {
		this.permission = permission
		usage = "/$fullName $_syntax"
	}
	var fullName: String = fullName
		private set

	// Is command registered in command map can only register once
	var registered = false
		set(value) {
			field = if (value) value else field
		}

	val syntax
		get() = StringUtil.processI18n(pluginRegistering, arrayOf(_syntax))

	val rawSyntax
		get() = LegacyComponentSerializer.legacySection().serialize(StringUtil.processI18n(pluginRegistering, arrayOf(_syntax)))

	val SOMETHING_WRONG = I18nMessage(pluginRegistering, "something-wrong")

	val preRunComponents = mutableListOf(
		PreRunPermissionCheck::class.java.getDeclaredConstructor(CommandLupi::class.java),
		PreRunConditionCheck::class.java.getDeclaredConstructor(CommandLupi::class.java),
		PreRunGetCommandParams::class.java.getDeclaredConstructor(CommandLupi::class.java),
		PreRunPickSubCommand::class.java.getDeclaredConstructor(CommandLupi::class.java)
	)

	val responseComponents = mutableListOf(
		ArrayResponseComponent::class.java.getDeclaredConstructor(CommandLupi::class.java),
		StringResponseComponent::class.java.getDeclaredConstructor(CommandLupi::class.java),
		TextCompResponse::class.java.getDeclaredConstructor(CommandLupi::class.java),
		I18nResponseComponent::class.java.getDeclaredConstructor(CommandLupi::class.java)
	)

	override fun getDescription(): String {
		return StringUtil.componentToString(
			StringUtil.processI18n(pluginRegistering, arrayOf(description))
		)
	}
	override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		if (!hasFlag(CommandFlag.ASYNC))
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

	fun hasFlag(flag: CommandFlag): Boolean {
		return flags.contains(flag)
	}

	private fun runCommand(sender: CommandSender, args: Array<out String>) {
		val clazz = declaringClazz
		val obj = clazz.getDeclaredConstructor()?.newInstance()
		if (obj == null) {
			SOMETHING_WRONG.send(sender)
			return
		}

		injectDependencies(obj)

		runCommand(sender, args, obj)
	}
	private fun runCommand(sender: CommandSender, args: Array<out String>, obj: Any) {
		var subCMD: CommandLupi? = null
		var cmdParams: MutableList<Any>? = null

		for(componentConstructor in preRunComponents) {
			val constructed = componentConstructor
				.newInstance(this)

			constructed.run(sender, args, obj)

			if(constructed.subCommand != null)
				subCMD = constructed.subCommand
			if(constructed.cmdParams != null)
				cmdParams = constructed.cmdParams

			if (constructed.isAborted())
				return
		}

		val fullNameCommand = KeyValueBinder("command", fullName)
		val syntax = KeyValueBinder("syntax", _syntax)

		val badArg = I18nMessage(pluginRegistering, "bad-arg", fullNameCommand, syntax)

		val isItCallForFunction = (subCMD == null || (subCommands.isNotEmpty() && args.isEmpty()) && hasDefault())

		if (isItCallForFunction) {
			if (cmdParams == null || method == null) {
				badArg.send(sender)
				return
			}

			val res = method.invoke(obj, *cmdParams.toTypedArray())
			if(res != null)
				sendResponse(sender, res)
			return
		}

		if (subCMD == null) {
			return
		}

		val parameterSize = cmdParams?.size ?: 0

		if(parameterSize-1 == args.size) {
			badArg.send(sender)
			return
		}

		val clazz = subCMD.declaringClazz
		val types = getParametersTypes()

		val inst: Any = obj

		if(cmdParams == null && declaringClazz == clazz) {
			subCMD.runCommand(sender, getArgs(parameterSize+1, args), inst)
		}
		else if(hasDefault()) {
			subCMD.runCommand(sender, getArgs(parameterSize,args), inst)
		}
		// Only should happen when has subclass sub-command
		// Worst thing only that could happen is saving variables in command and not having an idea that it passes also fields that are not listed as @Dependency
		else if(cmdParams == null) {
			val fields = declaringClazz.declaredFields.filter { !it.isAnnotationPresent(Dependency::class.java) }.filterNotNull()
			if (fields.isEmpty()) {
				subCMD.runCommand(sender, getArgs(parameterSize+1, args))
				return
			}
			val fieldValues = fields.mapNotNull {
				it.isAccessible = true
				val out = it.get(obj)
				it.isAccessible = false

				out
			}.toMutableList()

			val fieldTypes = fields.mapNotNull {
				it.type
			}.toMutableList()

			val constructed = getInstanceOfClazz(clazz, fieldTypes, fieldValues, false) ?: throw Error("You should pass your command arguments")

			subCMD.injectDependencies(constructed)

			subCMD.runCommand(sender, getArgs(parameterSize+1, args), constructed)
		}
		else {
			val constructed = getInstanceOfClazz(clazz, types, cmdParams) ?: return

			subCMD.injectDependencies(constructed)

			subCMD.runCommand(sender, getArgs(parameterSize, args), constructed)
		}
	}

	private fun hasDefault(): Boolean {
		return method?.isAnnotationPresent(Default::class.java) == true
	}

	// Given the following input:
	// subCommand1 args1 args2 subCommand2 args1 args2 subCommand3 args1 args2
	// subCommands offset is dependent on the number of args in the subCommand
	private fun getInstanceOfClazz(clazz: Class<*>, types: MutableList<out Class<*>>, cmdParams: MutableList<Any>, isPlayerFirst: Boolean = true): Any? {

		// Remove first type that is the player
		// Player type is useless to us because it should be parsed anyway
		// To the method because it needs to be first argument
		if(isPlayerFirst)
			cmdParams.removeFirst()

		val constructor: Constructor<*> =
		try {
			clazz.getDeclaredConstructor(*types.toTypedArray())
		}
		catch(ex: Exception) {
			ex.printStackTrace()
			return null
		} ?: return null

		return constructor.newInstance(*cmdParams.toTypedArray())
	}

	fun getParametersTypes(): MutableList<out Class<*>> {
		val args = mutableListOf<Class<*>>()
		for (parameter in parameters) {
			args.add(parameter.clazz)
		}
		return args
	}

	private fun sendResponse(sender: CommandSender, res: Any) {
		for (constructor in responseComponents) {
			val constructed = constructor.newInstance(this)
			if(!constructed.compare(res))
				continue
			if (res == Component.empty())
				break
			val message = constructed.run(res)
			sender.sendMessage(message)
			break
		}

	}


	fun tabComplete(sender: CommandSender, args: List<String>): MutableList<String> {
		var tabComplete = suggestSubCommand(sender, args)
		// If method is null then it's a sup command
		// Should be then tab completed from the suggested sub command tab complete
		if (args.size > parameters.size || method == null)
			return tabComplete

		// Parameter index is the last index of the arguments
		val paramIDX = args.size - 1
		if(paramIDX < 0)
			return tabComplete

		val parameter = getParameter(args) ?: return tabComplete

		val autoComplete = parameter.autoComplete(sender, *getArgumentsDependingOnArgumentSpan(args))
		tabComplete.addAll(autoComplete)

		for (filter in filters) {
			tabComplete = filter.filterTabComplete(sender, this, parameter, tabComplete)
		}

		return tabComplete
	}
	fun getParameter(args: List<String>): ArgumentType? {
		var argSize = args.size
		for (parameter in parameters) {
			val argumentSpan = parameter.argumentSpan
			val alpha = if (argumentSpan == -1) 1 else argSize-argumentSpan
			if (alpha <= 0)
				return parameter
			argSize -= argumentSpan
		}
		return null
	}

	fun getArgumentsDependingOnArgumentSpan(args: Collection<String>): Array<String> {
		val argSize = args.size
		var alpha = 0
		for (parameter in parameters) {
			val argumentSpan = parameter.argumentSpan
			alpha += if (argumentSpan == -1) 1 else argumentSpan
			if (alpha >= argSize) {
				return args
					.toTypedArray()
					.copyOfRange(alpha-argumentSpan, argSize)
			}
		}
		return arrayOf()
	}

	fun suggestSubCommand(sender: CommandSender, args: List<String>): MutableList<String> {
		val parameters = getParametersTypes()

		val parSize = parameters.size
		if (parSize >= args.size)
			return mutableListOf()

		val commandArg = args[parSize]
		val commandList = mutableListOf<String>()

		if (hasFlag(CommandFlag.HELP)) {
			val commandName = I18nMessage(pluginRegistering, "help-command-name").getI18nResponse()
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

			val lowerCaseName = name.lowercase()
			val lowerCaseCommandArg = commandArg.lowercase()
			val aliasesStartingWith = subCommand.aliases.filter { it.startsWith(lowerCaseCommandArg) }.filterNotNull()
			if (lowerCaseName.startsWith(lowerCaseCommandArg) || aliasesStartingWith.isNotEmpty() ) {
				val res = suggestSubCommandParse(parSize, args, sender)
				if(res != null)
					return res
				commandList.add(name.lowercase())
				commandList.addAll(aliasesStartingWith)
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
			val subCommand = CommandUtil.resolveSubCommand(sender, this, args.toTypedArray()) ?: return null
			val offset = parSize+1
			return subCommand.tabComplete(sender, getArgs(offset, args.toTypedArray()).toList())
		}
		return null
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
		var endOffset = args.size
		val arguments = Array<Any>(endOffset-offset) { "$it" }
		System.arraycopy(args, offset, arguments, 0, endOffset-offset)
		return arguments
	}
	fun getNameSpace(): NamespacedKey {
		val name = this.permission ?: this.fullName.replace(' ', '_')
		return NamespacedKey(this.pluginRegistering, name)
	}

	override fun toString(): String {
		return "Command : " +
				"\n\tName: $fullName," +
				"\n\tDesc: $description," +
				"\n\tSyntax: $_syntax" +
				",\n\tMethod: ${method?.name}" +
				",\n\tPermission: $permission" +
				"\n"
	}

	private fun getGson(): Gson {
		return GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(CommandLupi::class.java, CommandLupiSerializer())
			.create()
	}

	fun toJson() : String {
		this.method?.isAccessible = false
		return getGson()
			.toJson(this)
	}


	fun toGsonTree(): JsonElement {
		return getGson()
			.toJsonTree(this)
	}

	private fun injectDependencies(obj: Any) {
		for (injectableDependency in injectableDependencies) {
			for (declaredField in obj::class.java.declaredFields) {
				if(!declaredField.isAnnotationPresent(Dependency::class.java)) continue

				if (injectableDependency.key != declaredField.type) continue

				declaredField.isAccessible = true

				declaredField.set(obj, injectableDependency.value)
			}
		}
		for(injectableDependency in namedInjectableDependencies) {
			for (declaredField in obj::class.java.declaredFields) {
				if(!declaredField.isAnnotationPresent(NamedDependency::class.java)) continue

				if (injectableDependency.key != declaredField.name) continue

				declaredField.isAccessible = true

				declaredField.set(obj, injectableDependency.value)
			}
		}
	}
}
