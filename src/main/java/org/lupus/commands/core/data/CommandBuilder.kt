package org.lupus.commands.core.data

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.apache.logging.log4j.Level
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.Dependency
import org.lupus.commands.core.annotations.NamedDependency
import org.lupus.commands.core.annotations.parameters.Optional
import org.lupus.commands.core.annotations.parameters.ParamName
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.lupus.commands.core.messages.I18nMessage
import org.lupus.commands.core.scanner.ClazzScanner
import org.lupus.commands.core.scanner.modifiers.AnyModifier
import org.lupus.commands.core.scanner.modifiers.FieldsModifier
import org.lupus.commands.core.scanner.modifiers.ParameterModifier
import org.lupus.commands.core.utils.LogUtil.outMsg
import org.lupus.commands.core.utils.StringUtil
import java.lang.reflect.Method
import java.lang.reflect.Parameter

open class CommandBuilder(
	var plugin: JavaPlugin,
	var name: String,
	val packageName: String,
	val declaringClazz: Class<*>,
	val parent: CommandBuilder? = null
) {
	private val pluginClazzLoader: ClassLoader = plugin::class.java.classLoader
	var noCMD: Boolean = false
	var namingSchema: Regex = Regex("Command|CMD")
		set(value) {
			field = value
			permission = getPerm()
		}
    var permission = getPerm()
	var description: String = ""
	val flags: MutableSet<CommandFlag> = hashSetOf()
	val filters: MutableList<FilterFun> = mutableListOf()


	var method: Method? = null
		set(value) {
			if (value == null)
				return
			field = value
			initializeMethodParameters(value)
		}

	val aliases: MutableList<String> = mutableListOf()
	var syntax = StringBuilder()

	val parameters: MutableList<ArgumentType> = mutableListOf()
	val subCommands: MutableList<CommandBuilder> = mutableListOf()
	val optionals = hashMapOf<Int, Array<String>>()
	val wildCards = mutableListOf<Int>()
	val cmdPasses = mutableListOf<Class<*>>()


	var supCommand: CommandBuilder? = null
		set(it) {
			if (it == null)
				return
			field = it
			this.permission = getPerm()
			if (it.hasFlag(CommandFlag.NO_PERM))
				this.flags.add(CommandFlag.NO_PERM)
		}
	var executorParameter: Parameter? = null
	var paramModifiers: List<ParameterModifier> = mutableListOf()
	var anyModifiers: List<AnyModifier> = mutableListOf()
	var fieldsModifiers: List<FieldsModifier> = mutableListOf()

	val conditions: MutableList<ConditionFun> = mutableListOf()
	val injectableDependencies: HashMap<Class<*>, Any> = hashMapOf()
	val namedInjectableDependencies: HashMap<String, Any> = hashMapOf()

	private val fieldScanAnnotationsFilter = listOf(
		Dependency::class.java,
		NamedDependency::class.java
	)

	fun runFieldScan() {
		for (declaredField in declaringClazz.declaredFields) {
			for (clazz in fieldScanAnnotationsFilter) {
				if (!declaredField.isAnnotationPresent(clazz)) continue
				val annotation = declaredField.getAnnotation(clazz)
				for (fieldModifier in fieldsModifiers) {
					if(fieldModifier.isThisAnnotationValid(annotation)) fieldModifier.modify(this, annotation, declaredField)
				}
			}
		}
	}

	var parameterCounter = 0
	fun addParameter(parameter: Parameter): CommandBuilder {

		val clazz = parameter.type
		var parameterName = parameter.getAnnotation(ParamName::class.java)?.paramName ?: parameter.name
		if(StringUtil.isThatI18nSyntax(parameterName)) {
			parameterName = LegacyComponentSerializer.legacySection().serialize(
				StringUtil.getI18nSyntax(plugin, listOf(parameterName)).getI18nResponse()
			)
		}

		val optional = parameter.getAnnotation(Optional::class.java) ?: null
		if(optional != null)
			optionals[parameterCounter] = optional.defaultValue.split("|").toTypedArray()
		parameterCounter++

		for (paramModifier in paramModifiers) {
			val ann = parameter.getAnnotation(paramModifier.annotation) ?: continue
			paramModifier.modify(this, ann, parameter)
		}
		for (modifier in anyModifiers) {
			val ann = parameter.getAnnotation(modifier.annotation) ?: continue
			modifier.modify(this, ann, parameter)
		}

		val argumentType = ArgumentTypeList[clazz]
			?: throw IllegalArgumentException("$clazz argument isn't in ArgumentTypes list https://github.com/LupusVirtute/LupusCMDFramework/wiki/Clazz-argument-isn't-in-ArgumentTypes-List")

		if (argumentType.argumentSpan > 1) {
			val argumentNames = argumentType.argumentName.split(',')

			this.parameters.add(argumentType)
			for (i in 0 until argumentType.argumentSpan) {
				if (optional == null)
					syntax.append("[${parameterName}.${argumentNames[i]}] ")
				else
					syntax.append("{${parameterName}.${argumentNames[i]}} ")
			}

		}
		else {
			this.parameters.add(argumentType)

			if (optional == null)
				syntax.append("[${parameterName}] ")
			else
				syntax.append("{${parameterName}} ")
		}
		return this
	}

	fun build(previousNameSpace: String = ""): List<CommandLupi> {
		var previousNameSpace = previousNameSpace

		val subCommands = mutableListOf<CommandLupi>()
		if (StringUtil.isThatI18nSyntax(name)) {
			this.name = StringUtil.getI18nSyntax(plugin, listOf(name)).getI18nUnformatted()
		}

		if (previousNameSpace.isEmpty())
			previousNameSpace = name

		for (subCommand in this.subCommands) {
			var nameSpace = previousNameSpace

			if (StringUtil.isThatI18nSyntax(subCommand.name)) {
				subCommand.name = StringUtil.getI18nSyntax(plugin, listOf(subCommand.name)).getI18nUnformatted()
			}

			if (!hasFlag(CommandFlag.CONTINUOUS) && !subCommand.hasFlag(CommandFlag.CONTINUOUS)) {
				nameSpace +=
					"${this.syntax} ${subCommand.name} "
				nameSpace = nameSpace
					// Replace double space if any exists
					.replace("  ", " ")
			}

			subCommand.injectableDependencies.putAll(this.injectableDependencies)
			subCommand.namedInjectableDependencies.putAll(this.namedInjectableDependencies)
			subCommands.addAll(subCommand.build(nameSpace))
			cmdPasses.addAll(subCommand.cmdPasses)
		}

		if (hasFlag(CommandFlag.CONTINUOUS))
			return subCommands

		var executor: ArgumentType? = null

		if (executorParameter != null)
			executor = ArgumentTypeList[executorParameter!!.type]

		if (subCommands.isNotEmpty()) {
			syntax.append(
				LegacyComponentSerializer
					.legacyAmpersand()
					.serialize(
						I18nMessage(plugin, "sub-name")
							.getI18nResponse()
					)
			)
		}

		val builtCommand = CommandLupi(
			name,
			description,
			syntax.toString(),
			aliases,
			subCommands,
			method,
			declaringClazz,
			parameters,
			plugin,
			executor,
			conditions,
			permission,
			previousNameSpace,
			flags,
			optionals,
			filters,
			injectableDependencies,
			namedInjectableDependencies,
			wildCards
		)

		outMsg(" ")
		outMsg(builtCommand.toString())
		outMsg(" ")

		return listOf(
			builtCommand
		)
	}

    fun addSubCommandPass(pass: String) {
		val subCommand = pluginClazzLoader.loadClass(pass) ?: return
		if (subCommand == declaringClazz) throw Error("Infinite loop detected https://github.com/LupusVirtute/LupusCMDFramework/wiki/Infinite-Loops")
		val cmd = ClazzScanner(plugin, packageName).scan(subCommand,true) ?: return
		cmd.supCommand = this
    	this.subCommands.add(cmd)
		this.cmdPasses.add(cmd.declaringClazz)
	}

	fun addConditions(conditions: MutableList<ConditionFun>) {
		this.conditions.addAll(conditions)
	}

	private fun hasFlag(flag: CommandFlag): Boolean {
		return flags.contains(flag)
	}
	private fun getPerm(): String {
		if(method != null)
			if (!hasFlag(CommandFlag.PERMISSION_OVERRIDE) && (hasFlag(CommandFlag.NO_PERM) || supCommand?.hasFlag(CommandFlag.NO_PERM) == true))
				return ""
		var perm = plugin.name
		val supCommandPrefix = supCommand?.permission ?: ""
		// It's sure to be the last
		val methodName = getPermMethodPart()

		if (supCommandPrefix.isNotEmpty()) {
			perm = "$supCommandPrefix$methodName"
			return perm.lowercase()
		}
		val clazzPrefix = getPermClazzPrefixPart()
		perm += clazzPrefix

		return perm.lowercase()
	}
	private fun getPermClazzPrefixPart(): String {
		val clazzPrefix = declaringClazz
			.name
			.removePrefix("$packageName.")
			.replace(namingSchema, "")

		return ".${clazzPrefix}"
	}
	private fun getPermMethodPart(): String {
		if (method == null)
			return ""
		var methodName = method?.name ?: ""
		methodName = if(methodName.isNotEmpty()) ".$methodName" else ""
		return methodName
	}


	/**
	 * Scans method for it's parameters and whether they can be casted to arguments
	 */
	private fun initializeMethodParameters(
		methodToReplace: Method,
	) {
		var first = true
		for (parameter in methodToReplace.parameters) {
			if (!ArgumentTypeList.contains(parameter.type)) {
				outMsg(
					"[LCF] ERROR: ${methodToReplace.name} Command argument isn't defined in ArgumentTypeList did you load your command arguments before scanning class?",
					Level.FATAL
				)
				outMsg("If not use @NotCMD", Level.FATAL)
				this.method = null
				return
			}

			if (first) {
				first = false
				if (!CommandSender::class.java.isAssignableFrom(parameter.type)) {
					outMsg("[LCF] First argument of method ${methodToReplace.name} is not Bukkit CommandSender aborting")
					this.method = null
					return
				}
				this.executorParameter = parameter
				continue
			}
			this.addParameter(parameter)
		}
		permission = getPerm()
	}


}
