package org.lupus.commands.core.commands

import org.bukkit.entity.Player
import org.lupus.commands.core.annotations.Conditions

class TestCommand {
	@Conditions("creative")
	fun kick(executor: Player, kicked: Player) {

	}
}