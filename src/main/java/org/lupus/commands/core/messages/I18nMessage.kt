package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class I18nMessage(val plugin: JavaPlugin?,val path: String, val args: TagResolver) {
    constructor(plugin: JavaPlugin?,path: String, vararg args: KeyValueBinder):
            this(plugin,
                path,
                args
                    .flatMap { it.get() }
                    .toTypedArray()
            )
    @Deprecated("Use key-value binders or resolvers please")
    constructor(plugin: JavaPlugin?, path: String, args: Array<out String>) :
            this(plugin,
                path,
                I18n.getTagResolver(args)
            )
    constructor(plugin: JavaPlugin?, path: String, args: Array<out TagResolver.Single>) :
            this(plugin,
                path,
                I18n.getTagResolver(args)
            )

    fun getI18nResponse(): Component {
        return I18n[plugin, path, args]
    }

    /**
     * Get non formatted message
     */
    fun getI18nUnformatted(): String {
        return I18n.getUnformatted(plugin, path)
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
