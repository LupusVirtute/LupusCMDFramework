package org.lupus.commands.core.commands.admin

import org.bukkit.entity.Player
import org.lupus.commands.core.annotations.CMDPass
import org.lupus.commands.core.annotations.Default

class AdminCommand {
	@CMDPass("org.lupus.commands.core.commands.admin.ModCMD")
	fun test(executor: Player, player: String): String {
		return "Testing 123456789 ${player} "
	}
}