package org.lupus.commands.core.components.command.response

import net.kyori.adventure.text.Component
import org.lupus.commands.core.data.CommandLupi

class TextCompResponse(command: CommandLupi) : CommandResponseComponent(command, Component::class.java) {
    override fun run(input: Any): Component {
        return input as Component
    }
}