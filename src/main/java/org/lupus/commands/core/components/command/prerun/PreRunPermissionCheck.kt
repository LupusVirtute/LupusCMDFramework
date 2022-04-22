package org.lupus.commands.core.components.command.prerun

import org.bukkit.command.CommandSender
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.messages.I18nMessage
import org.lupus.commands.core.messages.KeyValueBinder

class PreRunPermissionCheck(command: CommandLupi) : PreRunCommandComponent(command) {
    val pluginRegistering = command.pluginRegistering
    val permission = command.permission

    override fun run(sender: CommandSender, args: Array<out String>, commandObj: Any) {
        val permissionBind = KeyValueBinder("permission", permission ?: "")
        val permissionMessage = I18nMessage(pluginRegistering, "no-perm", permissionBind)

        if(!sender.isOp && !command.testPermissionSilent(sender)) {
            permissionMessage.send(sender)
            this.abortExecution = true
            return
        }
    }

}