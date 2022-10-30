package org.lupus.commands.core.components.command.response

import net.kyori.adventure.text.Component
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.messages.I18nMessage

class I18nResponseComponent(command: CommandLupi) : CommandResponseComponent(command, I18nMessage::class.java) {
	override fun run(input: Any): Component {
		if(!compare(input))
			return Component.empty()
		input as I18nMessage
		return input.getI18nResponse()
	}
}
