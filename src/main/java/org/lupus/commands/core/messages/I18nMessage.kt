package org.lupus.commands.core.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class I18nMessage(val plugin: JavaPlugin?,val path: String, val args: MutableList<TagResolver>) {

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
    constructor(plugin: JavaPlugin?, path: String) :
            this(plugin, path, TagResolver.empty())

    constructor(plugin: JavaPlugin?, path: String, args: TagResolver) :
            this(plugin, path, mutableListOf(args))

    fun getI18nResponse(): Component {
        return I18n[plugin, path, I18n.getTagResolver(args.toTypedArray())]
    }

    fun getI18nResponseTranslated(locale: String): Component {
        return I18n[plugin, path, locale, I18n.getTagResolver(args.toTypedArray())]
    }

    /**
     * ### Splits message into list of components by spliting unformatted string by endline character '\\n'
     * #### Example: '<red>Hello\\n world' turns into
     * ```
     * <red>Hello
     * world
     * ```
     * #### 'world' word will be white whilst Hello will be red
     */
    fun getI18nResponseList(): List<Component> {
        return getI18nUnformatted().split("\\n")
            .map {
                MiniMessage
                    .miniMessage()
                    .deserialize(
                        it,
                        I18n.getTagResolver(args.toTypedArray())
                    )
            }
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

    fun addPlaceholder(placeholder: TagResolver.Single) {
        args.add(placeholder)
    }
    fun addPlaceholders(placeholders: Collection<TagResolver.Single>) {
        args.addAll(placeholders)
    }

    /**
     * Sends message based on player's locale
     */
    fun sendTranslated(receiver: Player) {
        val language = receiver.locale().language
        receiver.sendMessage(getI18nResponseTranslated(language))
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
