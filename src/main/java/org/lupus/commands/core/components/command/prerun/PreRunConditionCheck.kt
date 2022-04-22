package org.lupus.commands.core.components.command.prerun

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.utils.StringUtil

class PreRunConditionCheck(command: CommandLupi) : PreRunCommandComponent(command) {
    override fun run(sender: CommandSender, args: Array<out String>, commandObj: Any) {
        for (condition in command.conditions) {
            val result = condition.run(sender, command, args as Array<Any>)
            if(!result) {
                when (val response = condition.getResponse(sender, command, args as Array<Any>)) {
                    is Component -> sender.sendMessage(response)
                    is String -> sender.sendMessage(response)
                    is Array<*> -> sender.sendMessage(StringUtil.processI18n(command.pluginRegistering, response as Array<String>))
                    else -> sender.sendMessage(response.toString())
                }
                this.abortExecution = true
                return
            }
        }
    }

}