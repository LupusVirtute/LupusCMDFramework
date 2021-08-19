package org.lupus.commands.core.commands.admin

import org.bukkit.entity.Player
import org.lupus.commands.core.annotations.Default

class AdminCommand {
	@Default
	fun test(executor: Player, player: Player): String {
		return "Testing 123456789 ${player.name}"
	}
}