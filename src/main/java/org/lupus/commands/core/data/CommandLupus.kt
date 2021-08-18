package org.lupus.commands.core.data

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType
import java.lang.reflect.Method

class CommandLupus(
	name: String,
	description: String,
	syntax: String,
	aliases: List<String>,
	val subCommands: List<CommandLupus>,
	val method: Method?,
	val parameters: List<ArgumentType>
) : Command(name, description, syntax, aliases)
{
	override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		if (subCommands.isNotEmpty()) {
			parseSubCommands(sender, commandLabel, args)
			return true
		}
		if (method == null) {
			return true
		}

		return true
	}

	private fun parseSubCommands(sender: CommandSender, commandLabel: String, args: Array<out String>) {
		val subArgument = args[0]
		for (subCommand in subCommands) {
			if (subCommand.aliases.contains(subArgument) || subCommand.name == subArgument) {
				dispatchCommandToSubCommand(sender, commandLabel, args, subCommand)
				return
			}
		}
	}

	fun dispatchCommandToSubCommand(sender: CommandSender, commandLabel: String, args: Array<out String>, subCommand: CommandLupus) {
		val argumentArray = Array(args.size-1) { "$it" }
		System.arraycopy(args, 1, argumentArray, 0, args.size-1)
		subCommand.execute(sender, commandLabel, argumentArray)
	}

	override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
		if(parameters.size < args.size) {
			return super.tabComplete(sender, alias, args)
		}
		val lastIDX = args.size-1
		val parameter = parameters[lastIDX]

		val lastOne = args[lastIDX]
		return parameter.autoComplete(sender, lastOne)
	}
}
