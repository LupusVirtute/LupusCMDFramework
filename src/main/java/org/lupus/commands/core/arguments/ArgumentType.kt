package org.lupus.commands.core.arguments

import org.bukkit.command.CommandSender

abstract class ArgumentType @JvmOverloads constructor(private val clazz: Class<out Any>, val argumentSpan: Int = 1, val argumentName: String = "") {
	abstract fun conversion(sender: CommandSender, vararg input: String): Any?
	abstract fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String>
	fun isTheArgumentOfThisType(clazz: Class<out Any>): Boolean {
		return this.clazz!!.isAssignableFrom(clazz)
	}
}