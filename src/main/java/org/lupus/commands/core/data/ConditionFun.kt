package org.lupus.commands.core.data

import org.bukkit.command.CommandSender
import org.lupus.commands.core.scanner.ClazzScanner

/**
 * The condition class you need to inherit it to start coding your own conditions
 */
abstract class ConditionFun {

	// The response player receives if condition failed
	open fun getResponse(sender: CommandSender, commandLupi: CommandLupi, args: Array<Any>): Any {
		return ""
	}

	abstract fun run(sender: CommandSender, commandLupi: CommandLupi, args: Array<Any>): Boolean
}