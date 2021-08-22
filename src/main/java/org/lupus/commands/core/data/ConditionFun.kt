package org.lupus.commands.core.data

import org.bukkit.command.CommandSender

/**
 * The condition class you need to inherit it to start coding your own conditions
 */
abstract class ConditionFun() {
	// The response player receives if condition failed
	open fun getResponse(sender: CommandSender, args: Array<out String>): String {
		return ""
	}

	abstract fun run(sender: CommandSender, args: Array<out String>): Boolean
}