package org.lupus.commands.core.components.command

import org.lupus.commands.core.components.Component
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.messages.I18nMessage

abstract class CommandComponent(val command: CommandLupi) : Component() {
    protected var abortExecution: Boolean = false
    protected var abortExecutionMessage: I18nMessage? = null

    fun isAborted(): Boolean {
        return abortExecution
    }
}