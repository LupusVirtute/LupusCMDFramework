package org.lupus.commands.core.components.command.response.arrays

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.lupus.commands.core.data.CommandLupi

class ComponentLikeResponse(override val command: CommandLupi) : ArrayResponseType {
	override fun check(array: Array<*>): Boolean {
		return array.first() is ComponentLike
	}

	override fun componentResponse(array: Array<*>): Component {
		return Component.join(
			JoinConfiguration.separator(
				MiniMessage.miniMessage().deserialize("\n")
			),
			(array as Array<out ComponentLike>).toList()
		)
	}
}
