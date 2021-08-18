package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object OfflinePlayerType : ArgumentType(OfflinePlayer::class.java) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getOfflinePlayerIfCached(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val players = mutableListOf<String>()
		for (offlinePlayer in Bukkit.getOfflinePlayers()) {

			val playerNick = offlinePlayer.name ?: continue
			if (playerNick.lowercase().startsWith(input[0].lowercase())) {
				players.add(playerNick)
			}
		}
		return players
	}
}