package org.lupus.commands.core.data

import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.arguments.ArgumentTypeList
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class CommandBuilder(var name: String, var method: Method) {

	val aliases: MutableList<String> = mutableListOf()
	val syntax = StringBuilder()
	val parameters: MutableList<ArgumentType> = mutableListOf()
	val subCommands: MutableList<CommandLupus> = mutableListOf()

	fun addAlias(vararg aliases: String): CommandBuilder {
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

	fun addSubCommand(sub: CommandLupus) {
		subCommands.add(sub)
	}

}