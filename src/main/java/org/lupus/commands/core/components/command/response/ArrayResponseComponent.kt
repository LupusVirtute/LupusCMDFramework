package org.lupus.commands.core.components.command.response

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.lupus.commands.core.components.command.response.arrays.ArrayResponseType
import org.lupus.commands.core.components.command.response.arrays.ComponentLikeResponse
import org.lupus.commands.core.components.command.response.arrays.StringResponse
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.utils.StringUtil

class ArrayResponseComponent(command: CommandLupi) : CommandResponseComponent(command, Array::class.java) {
	val arrayResponseTypes = mutableListOf(ComponentLikeResponse(command), StringResponse(command))

    override fun run(input: Any): Component {
        input as Array<*>
		for (arrayResponseType in arrayResponseTypes) {
			if (arrayResponseType.check(input)) {
				return arrayResponseType.componentResponse(input)
			}
		}
        return Component.text("")
    }

}
