package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class I18nMessage(val plugin: JavaPlugin?,val path: String, val args: Array<String>) {
    constructor(plugin: JavaPlugin?,path: String, vararg args: KeyValueBinder):
            this(plugin,
                path,
                args
                    .flatMap { it.get() }
                    .toTypedArray()
            )

    fun getI18nResponse(): Component {
        return I18n.get(plugin, path, *args)
    }
    fun getI18nUnformatted(): String {
        return I18n.getUnformatted(plugin, path, *args)
    }

    fun send(receiver: CommandSender) {
        receiver.sendMessage(getI18nResponse())
    }
    fun sendIfNotEmpty(receiver: CommandSender) {
        if (getI18nUnformatted().isNotEmpty()) {
            send(receiver)
        }
    }

    fun sendUnFormatted(receiver: CommandSender) {
        receiver.sendMessage(getI18nUnformatted())
    }
}
