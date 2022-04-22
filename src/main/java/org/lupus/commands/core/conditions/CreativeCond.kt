package org.lupus.commands.core.conditions

import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.data.ConditionFun
import org.lupus.commands.core.managers.ConditionManager
import org.lupus.commands.core.messages.I18n
import org.lupus.commands.core.messages.I18nMessage

object CreativeCond : ConditionFun() {
	override fun run(sender: CommandSender, commandLupi: CommandLupi, args: Array<Any>): Boolean {
		if (sender !is Player)
			return false
		return sender.gameMode == GameMode.CREATIVE
	}

	override fun getResponse(sender: CommandSender, commandLupi: CommandLupi, args: Array<Any>): Any {
		val message = I18nMessage(commandLupi.pluginRegistering, "creative-cond-response")
		return message.getI18nResponse()
	}
}