package org.lupus.commands.core.data

import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.CMDPass
import org.lupus.commands.core.annotations.ParamName
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.lupus.commands.core.scanner.ClazzScanner
import org.lupus.commands.core.scanner.modifiers.AnyModifier
import org.lupus.commands.core.scanner.modifiers.BaseModifier
import org.lupus.commands.core.scanner.modifiers.ParameterModifier
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class CommandBuilder(
	var plugin: JavaPlugin,
	var name: String,
	val packageName: String,
	val declaringClazz: Class<*>
) {
	private val pluginClazzLoader: ClassLoader = plugin::class.java.classLoader
	var noCMD: Boolean = false
    var permission = ""
	private var fullName = name
	var description: String = ""
	var method: Method? = null
	val aliases: MutableList<String> = mutableListOf()
	var syntax = StringBuilder()

	val parameters: MutableList<ArgumentType> = mutableListOf()
	val subCommands: MutableList<CommandBuilder> = mutableListOf()

	var help: Boolean = false
	var async: Boolean = false
	var continuous: Boolean = false


	var supCommand: CommandBuilder? = null
	var executorParameter: Parameter? = null
	val paramModifiers: MutableList<ParameterModifier> = mutableListOf()
	val anyModifiers: MutableList<AnyModifier> = mutableListOf()
	val conditions: MutableList<ConditionFun> = mutableListOf()

	init {

	}


	fun addParameter(parameter: Parameter): CommandBuilder {
		val clazz = parameter.type
		val parameterName = parameter.getAnnotation(ParamName::class.java)?.paramName ?: parameter.name

		for (paramModifier in paramModifiers) {
			val ann = parameter.getAnnotation(paramModifier.annotation) ?: continue
			paramModifier.modify(this, ann, parameter)
		}
		for (modifier in anyModifiers) {
			val ann = parameter.getAnnotation(modifier.annotation) ?: continue
			modifier.modify(this, ann, parameter)
		}

		val argumentType = ArgumentTypeList[clazz]
			?: throw IllegalArgumentException("clazz argument isn't ArgumentType")


		if (argumentType.argumentSpan > 1) {
			val argumentNames = argumentType.argumentName.split(',')

			for (i in 0..argumentType.argumentSpan) {
				this.parameters.add(argumentType)
				syntax.append("[${parameterName}.${argumentNames[i]}] ")
			}

		}
		else {
			this.parameters.add(argumentType)

			syntax.append("[${parameterName}] ")
		}
		return this
	}

	fun build(): List<CommandLupi> {

		val subCommands = mutableListOf<CommandLupi>()
		for (subCommand in this.subCommands) {
			if (!continuous && !subCommand.continuous)
				subCommand.fullName = """${this.fullName} ${this.syntax} ${subCommand.name}"""
			subCommands.addAll(subCommand.build())
		}
		if (continuous)
			return subCommands
		var executor: ArgumentType? = null
		if (executorParameter != null)
			executor = ArgumentTypeList[executorParameter!!.type]
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
			fullName,
			help,
			async,
			supCommand != null
		)
		println(" ")
		println(builtCommand.toString())
		println(" ")
		return listOf(
			builtCommand
		)
	}

    fun addPass(pass: String) {
		val subCommand = getCommandPass(method) ?: return
		val cmd = ClazzScanner(subCommand, plugin, packageName).scan(true) ?: return
    	this.subCommands.add(cmd)
	}
	private fun getCommandPass(method: Method?): Class<*>? {
		if (method == null)
			return null
		val cmdPass = method.getAnnotation(CMDPass::class.java)?.commandPath ?: return null
		return pluginClazzLoader.loadClass(cmdPass)
	}

	fun addConditions(conditions: MutableList<ConditionFun>) {
		this.conditions.addAll(conditions)
	}


}