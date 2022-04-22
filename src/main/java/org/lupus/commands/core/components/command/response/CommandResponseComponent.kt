package org.lupus.commands.core.components.command.response

import net.kyori.adventure.text.Component
import org.lupus.commands.core.components.command.CommandComponent
import org.lupus.commands.core.data.CommandLupi

abstract class CommandResponseComponent(command: CommandLupi, val clazz: Class<*>) : CommandComponent(command) {
    abstract fun run(input: Any): Component
    fun compare(input: Any): Boolean {
        return clazz.isInstance(input)
    }
}