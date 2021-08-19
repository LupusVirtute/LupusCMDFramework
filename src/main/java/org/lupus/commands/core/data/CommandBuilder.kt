package org.lupus.commands.core.data

import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.arguments.ArgumentTypeList
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class CommandBuilder(var plugin: JavaPlugin, var name: String, var description: String, var method: Method?, val async: Boolean = false) {

	val aliases: MutableList<String> = mutableListOf()
	val syntax = StringBuilder()
	val parameters: MutableList<ArgumentType> = mutableListOf()
	val subCommands: MutableList<CommandLupi> = mutableListOf()

	fun addAlias(aliases: List<String>): CommandBuilder {
		this.aliases.addAll(aliases)
		return this
	}
	fun addParameter(parameter: Parameter): CommandBuilder {
		val clazz = parameter.type
		val argumentType = ArgumentTypeList[clazz]
			?: throw IllegalArgumentException("clazz argument isn't ArgumentType")
		if (argumentType.argumentSpan > 1) {
			val argumentNames = argumentType.argumentName.split(',')
			for (i in 0..argumentType.argumentSpan) {
				this.parameters.add(argumentType)
				syntax.append("[${parameter.name}.${argumentNames[i]} ")
			}
		}
		else {
			this.parameters.add(argumentType)
			syntax.append("[${parameter.name}] ")
		}
		return this
	}
	fun addParameters(parameters: List<Parameter>):CommandBuilder {
		for (parameter in parameters) {
			addParameter(parameter)
		}
		return this
	}

	fun addSubCommand(sub: CommandLupi) {
		subCommands.add(sub)
	}
	fun addSubCommands(sub: List<CommandLupi>) {
		for (commandLupi in sub) {
			addSubCommand(commandLupi)
		}
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
			async
		)
	}

}