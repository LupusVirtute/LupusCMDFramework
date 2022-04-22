package org.lupus.commands.core.components.command.response

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.utils.StringUtil

class ArrayResponseComponent(command: CommandLupi) : CommandResponseComponent(command, Array::class.java) {
    override fun run(input: Any): Component {
        input as Array<*>
        if (input.first() is String) {
            if (!StringUtil.isThatI18nSyntax(input.first() as String)) {
                return MiniMessage.miniMessage().deserialize(input.first() as String)
            }

            return StringUtil.getI18nSyntax(command.pluginRegistering, (input as Array<String>).toList()).getI18nResponse()
        }
        return Component.text("")
    }

}