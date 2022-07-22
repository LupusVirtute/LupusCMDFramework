package org.lupus.commands.core.components.command.prerun

import org.bukkit.command.CommandSender
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.messages.I18nMessage
import org.lupus.commands.core.messages.KeyValueBinder
import org.lupus.commands.core.utils.ArrayUtil

class PreRunGetCommandParams(command: CommandLupi) : PreRunCommandComponent(command) {
    override fun run(sender: CommandSender, args: Array<out String>, commandObj: Any) {
        this.cmdParams = getCommandParameters(sender, args)
    }
    private fun getCommandParameters(sender: CommandSender, args: Array<out String>): MutableList<Any>? {
        val arguments = mutableListOf<Any>()
        val commandFullName = KeyValueBinder("command",command.fullName)
        val commandSyntax = KeyValueBinder("syntax",command.rawSyntax)
        val notForThisType = I18nMessage(command.pluginRegistering, "not-for-type", commandFullName, commandSyntax)

        if(command.executor == null)
            return null
        if (!command.executor.isTheArgumentOfThisType(sender::class.java)) {
            notForThisType.send(sender)
            return null
        }
        // Add sender as argument because
        // first argument is always the sender
        arguments.add(sender)

        var argumentSpanCounter = 0
        var iterCount = 0
        for (parameter in command.parameters) {
            var argumentSpan = parameter.argumentSpan

            val isInfinite = argumentSpan <= -1

            // We need to take every element left because of
            // Infinity = span is considered to the end of the arguments
            argumentSpan = if (isInfinite) 1 else argumentSpan

            argumentSpanCounter += if (isInfinite) 1 else parameter.argumentSpan
            val endOffset = if (isInfinite) args.size else argumentSpanCounter
            
            val value: Any =
                try {
                    val argumentsForParameter = command.optionals[iterCount] ?: ArrayUtil.getArgs(argumentSpanCounter-argumentSpan, endOffset, args)

                    parameter.conversion(sender, *argumentsForParameter)
                } catch(ex: Exception) {
                    null
                } ?: return null

            arguments.add(
                value
            )

            iterCount++

            if (isInfinite)
                break
        }
        return arguments
    }
}