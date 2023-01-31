package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.lupus.commands.core.arguments.ArgumentType


object PlayerType : ArgumentType(Player::class.java, canBeWildCard = true) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getPlayer(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val playerList = mutableListOf<String>()
		playerList += "*"
		for (onlinePlayer in Bukkit.getOnlinePlayers()) {
			if (isVanished(onlinePlayer) && !sender.isOp)
				continue
			if (onlinePlayer.name.lowercase().startsWith(input[0].lowercase())) {
				playerList.add(onlinePlayer.name)
			}
		}
		return playerList
	}
	private fun isVanished(player: Player): Boolean {
		for (meta in player.getMetadata("vanished")) {
			if (meta.asBoolean()) return true
		}
		return false
	}

	public override fun wildcard(sender: CommandSender, vararg input: String): MutableList<Any?> {
		return Bukkit.getOnlinePlayers().toMutableList()
	}
}
