package org.lupus.commands.core.data

import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.ParamName
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.arguments.ArgumentTypeList
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class CommandBuilder(
	var plugin: JavaPlugin,
	var name: String,
	var description: String,
	var method: Method?,
	val help: Boolean = false,
	val async: Boolean = false,
	val subCommand: Boolean = false
) {
	private var permission = ""
	private var fullName = ""
	private val aliases: MutableList<String> = mutableListOf()
	private var syntax = StringBuilder()
	private val parameters: MutableList<ArgumentType> = mutableListOf()
	private val subCommands: MutableList<CommandLupi> = mutableListOf()

	fun addAlias(aliases: List<String>): CommandBuilder {
		this.aliases.addAll(aliases)
		return this
	}

	private var firstParameter: Boolean = true

	fun addParameter(parameter: Parameter): CommandBuilder {
		val clazz = parameter.type
		val parameterName = parameter.getAnnotation(ParamName::class.java)?.paramName ?: parameter.name

		val argumentType = ArgumentTypeList[clazz]
			?: throw IllegalArgumentException("clazz argument isn't ArgumentType")
		if (argumentType.argumentSpan > 1) {
			val argumentNames = argumentType.argumentName.split(',')
			for (i in 0..argumentType.argumentSpan) {
				this.parameters.add(argumentType)
				if(!firstParameter)
					syntax.append("[${parameterName}.${argumentNames[i]} ")
				else
					firstParameter = false
			}
		}
		else {
			this.parameters.add(argumentType)
			if(!firstParameter)
				syntax.append("[${parameterName}] ")
			else
				firstParameter = false
		}
		return this
	}

	fun addParameters(parameters: List<Parameter>):CommandBuilder {
		for (parameter in parameters) {
			addParameter(parameter)
		}
		return this
	}

	fun addSubCommand(sub: CommandLupi): CommandBuilder {
		subCommands.add(sub)
		return this
	}

	fun addSubCommands(sub: List<CommandLupi>): CommandBuilder {
		for (commandLupi in sub) {
			addSubCommand(commandLupi)
		}
		return this
	}

	fun setPermission(permission: String): CommandBuilder {
		this.permission = permission
		return this
	}
	fun setFullName(fullName: String): CommandBuilder {
		this.fullName = fullName
		return this
	}

	fun setSyntax(syntax: String) {
		this.syntax = StringBuilder(syntax)
	}

	fun build(): CommandLupi {
		return CommandLupi(
			name,
			description,
			syntax.toString(),
			aliases,
			subCommands,
			method,
			parameters,
			plugin,
			permission,
			fullName,
			help,
			async,
			subCommand
		)
	}

}