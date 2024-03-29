package org.lupus.commands.core.components.command.prerun

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.lupus.commands.core.data.CommandFlag
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.messages.I18nMessage
import org.lupus.commands.core.messages.KeyValueBinder
import org.lupus.commands.core.utils.CommandUtil

class PreRunPickSubCommand(command: CommandLupi) : PreRunCommandComponent(command) {
    val helpCommand = I18nMessage(command.pluginRegistering, "help-command-name")

    override fun run(sender: CommandSender, args: Array<out String>, commandObj: Any) {
        this.subCommand = resolveSubCommand(sender, args)
        if(this.subCommand == null && this.abortExecution && command.hasFlag(CommandFlag.HELP)) {
            parseHelp(sender)
        }
    }

    fun resolveSubCommand(sender: CommandSender, args: Array<out String>): CommandLupi? {
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

        if (command.hasFlag(CommandFlag.HELP)) {
            val plainTextCMD = helpCommand.getI18nUnformatted()
            if (commandArg == plainTextCMD) {
                this.abortExecution = true
                return null
            }
        }


        return CommandUtil.resolveSubCommand(sender, command, commandArg)
    }

    fun parseHelp(sender: CommandSender): Boolean {
        val commandName = KeyValueBinder("command", command.name)
        val preFix = I18nMessage(command.pluginRegistering, "prefix-help", commandName)
        preFix.sendIfNotEmpty(sender)
        var idx = 1
        for (subCommand in command.subCommands) {
            val syntax = subCommand.syntax

			val isDescriptionI18nComponent = subCommand.isDescriptionI18nComponent

            val commandFullNameBinded = Placeholder.parsed("command", "${command.name} ${subCommand.name}")

			val syntaxKeyValue = Placeholder.component("syntax", syntax)

			val descKeyValue =
				if(isDescriptionI18nComponent) Placeholder.component("desc", subCommand.descriptionComponent)
				else Placeholder.parsed("desc", subCommand.description)

			val idxKeyValue = Placeholder.parsed("idx", idx.toString())

            val i18nHelp = I18nMessage(command.pluginRegistering, "help-syntax", arrayOf(
					commandFullNameBinded,
					syntaxKeyValue,
					descKeyValue,
					idxKeyValue
				))

            i18nHelp.send(sender)
            idx++
        }
        val postFix = I18nMessage(command.pluginRegistering, "postfix-help", commandName)
        postFix.sendIfNotEmpty(sender)
        return true
    }
}
