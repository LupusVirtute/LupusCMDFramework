package org.lupus.commands.core.data

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.utils.ReflectionUtil.getPrivateField
import java.lang.reflect.Method

class CommandLupi(
	name: String,
	description: String,
	syntax: String,
	aliases: List<String>,
	val subCommands: List<CommandLupi>,
	val method: Method?,
	val parameters: List<ArgumentType>,
	val badSubCommand: TextComponent = Component.text("Bad sub command argument").color(RED),
	val badArgument: TextComponent = Component.text("Bad argument nr %s").color(RED),
	// Launches when bad sender executes command
	val notForThisSender: TextComponent = Component.text("You are not allowed to access this command").color(RED)
) : Command(name, description, syntax, aliases)
{
	companion object {
		val RED = TextColor.color(255,0,0)
	}
	override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		if (subCommands.isNotEmpty()) {
			val result = parseSubCommands(sender, commandLabel, args)
			if (result)
				return true
		}
		if (method == null) {
			return true
		}
		runCommand(sender, args)
		return true
	}

	private fun parseSubCommands(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		if(args.isEmpty())
			return false
		val subArgument = args[0]
		for (subCommand in subCommands) {
			if (subCommand.aliases.contains(subArgument) || subCommand.name == subArgument) {
				dispatchCommandToSubCommand(sender, commandLabel, args, subCommand)
				return true
			}
		}
		return false
	}

	private fun dispatchCommandToSubCommand(sender: CommandSender, commandLabel: String, args: Array<out String>, subCommand: CommandLupi) {
		val argumentArray = Array(args.size-1) { "$it" }
		System.arraycopy(args, 1, argumentArray, 0, args.size-1)
		subCommand.execute(sender, commandLabel, argumentArray)
	}

	private fun runCommand(sender: CommandSender, args: Array<out String>) {
		if (method == null) {
			return
		}

		val clazz = method.declaringClass
		val obj = clazz.getConstructor().newInstance()
		// Minuse one because we take into account player
		if (args.size < parameters.size-1) {
			var betterContent = badArgument.content()
			betterContent = betterContent.format(parameters.size - args.size).replace(' ', ' ')
			sender.sendMessage(badArgument.content(betterContent))
			return
		}
		var first = true
		var iteration = 0
		val arguments = arrayListOf<Any>()
		for (parameter in parameters) {
			if (first){
				if (!parameter.clazz.isAssignableFrom(sender::class.java)) {
					sender.sendMessage(notForThisSender)
					return
				}
				first = false
				arguments.add(sender)
				continue
			}
			val value = parameter.conversion(sender, args[iteration])
			iteration++
			if (value == null) {
				sender.sendMessage(badArgument)
				return
			}
			arguments.add(
				value
			)
		}
		val argArray: Array<Any> = arguments.toArray()
		val response = method.invoke(obj, *argArray) ?: return
		sendResponse(sender, response)
	}
	fun sendResponse(sender: CommandSender, res: Any) {
		if (res is String)
			sender.sendMessage(res)
		if (res is TextComponent)
			sender.sendMessage(res)
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
	fun registerCommand(plugin: JavaPlugin) {
		try {
			val result: Any = getPrivateField(
				Bukkit.getServer().pluginManager,
				"commandMap"
			)

			val commandMap = result as SimpleCommandMap
			commandMap.register(super.getName(), plugin.name, this)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
