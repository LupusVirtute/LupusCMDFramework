package org.lupus.commands.core.conditions

import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.lupus.commands.core.data.ConditionFun
import org.lupus.commands.core.managers.ConditionManager

object CreativeCond : ConditionFun() {
	override fun run(sender: CommandSender, args: Array<out String>): Boolean {
		if (sender !is Player)
			return false
		return sender.gameMode == GameMode.CREATIVE
	}

	override fun getResponse(sender: CommandSender, args: Array<out String>): String {
		return "You aren't in creative mode"
	}
}