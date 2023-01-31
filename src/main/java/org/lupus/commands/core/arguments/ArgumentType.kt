package org.lupus.commands.core.arguments

import org.bukkit.command.CommandSender

abstract class ArgumentType @JvmOverloads constructor(
	val clazz: Class<out Any>,
	/**
	 * argument span the amount of strings it takes
	 * to parse the argument
	 * -1 = infinite
	 */
    var argumentSpan: Int = 1,
	/**
	 * Argument name to display in help
	 */
	var argumentName: String = "",
	/**
	 * Is this wildcard instead of conversion
	 * The wildcard function will be used
	 */
 	var canBeWildCard: Boolean = false) {


	abstract fun conversion(sender: CommandSender, vararg input: String): Any?
	abstract fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String>
	fun isTheArgumentOfThisType(clazz: Class<out Any>): Boolean {
		return this.clazz.isAssignableFrom(clazz)
	}

	fun resolveWildcard(sender: CommandSender, vararg input: String): Any? {
		val firstInput = input[0]
		return if(firstInput == "*")
			wildcard(sender, *input)
		else
			conversion(sender, *input)
	}

	open fun wildcard(sender: CommandSender, vararg input: String): MutableList<Any?> {
		return mutableListOf()
	}
}
