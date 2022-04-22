package org.lupus.commands.core.components.command.response

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.utils.StringUtil

class StringResponseComponent(command: CommandLupi) : CommandResponseComponent(command, String::class.java) {
    override fun run(input: Any): Component {
        input as String
        if (StringUtil.isThatI18nSyntax(input)) {
            return StringUtil.getI18nSyntax(this.command.pluginRegistering, mutableListOf(input)).getI18nResponse()
        }
        return MiniMessage.miniMessage().deserialize(input)
    }
}