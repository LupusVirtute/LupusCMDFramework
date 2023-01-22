package org.lupus.commands.core.components.command.response.arrays

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.utils.StringUtil

class StringResponse(override val command: CommandLupi) : ArrayResponseType {

	override fun check(array: Array<*>): Boolean {
		return array.first() is String
	}

	override fun componentResponse(array: Array<*>): Component {
		if (!StringUtil.isThatI18nSyntax(array.first() as String)) {
			return MiniMessage.miniMessage().deserialize(array.first() as String)
		}

		return StringUtil.getI18nSyntax(command.pluginRegistering, (array as Array<String>).toList()).getI18nResponse()
	}
}
