package org.lupus.commands.core.commands.admin

import org.bukkit.entity.Player
import org.lupus.commands.core.annotations.Default
import org.lupus.commands.core.annotations.SubCommand

@SubCommand
class ModCMD(val player: String) {
	@Default
	fun test(executor: Player): String {
		return "$player Back from future"
	}

}