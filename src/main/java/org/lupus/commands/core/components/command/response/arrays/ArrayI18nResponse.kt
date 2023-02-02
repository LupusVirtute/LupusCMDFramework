package org.lupus.commands.core.components.command.response.arrays

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.messages.I18nMessage

class ArrayI18nResponse(override val command: CommandLupi) : ArrayResponseType {

	override fun check(array: Array<*>): Boolean {
		return array.first() is I18nMessage
	}

	override fun componentResponse(array: Array<*>): Component {
		return Component.join(
			JoinConfiguration.separator(
				MiniMessage.miniMessage().deserialize("\n")
			),
			(array as Array<out I18nMessage>)
				.map { it.getI18nResponse() }
				.toList()
		)
	}
}
