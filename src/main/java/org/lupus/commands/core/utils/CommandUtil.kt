package org.lupus.commands.core.utils

import org.bukkit.command.CommandSender
import org.lupus.commands.core.data.CommandLupi

object CommandUtil {
    fun resolveSubCommand(sender: CommandSender, command: CommandLupi, args: Array<out String>): CommandLupi? {
            val parameters = command.getParametersTypes()

            val parSize = parameters.size
            // If args are less or equal to parameters, it means that user isn't trying to use the sub-command
            // /command subCommand arg1 arg2 ... argN
            //                     0    1        n
            // args.size = n + 1
            // parameters.size = n
            if (args.size < parSize+1)
                return null
        val commandArg = args[parSize].lowercase()

        return resolveSubCommand(sender, command, commandArg)
    }
    fun resolveSubCommand(sender: CommandSender, command: CommandLupi, commandArg: String): CommandLupi? {
        for (subCommand in command.subCommands) {
            if (!subCommand.testPermissionSilent(sender)) {
                continue
            }
            val name = subCommand.name
            if (name.lowercase() == commandArg) {
                return subCommand
            }
        }
        return null
    }
}