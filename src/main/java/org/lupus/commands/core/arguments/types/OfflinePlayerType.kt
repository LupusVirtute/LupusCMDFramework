package org.lupus.commands.core.arguments.types

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

/**
 * Gets player regardless if he is online or offline
 */
object OfflinePlayerType : ArgumentType(OfflinePlayer::class.java, canBeWildCard = true) {
	override fun conversion(sender: CommandSender, vararg input: String): Any? {
		return Bukkit.getOfflinePlayerIfCached(input[0])
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val players = mutableListOf<String>()
		players += "*"
		val everyPlayer = Bukkit.getOfflinePlayers().toMutableSet()
		everyPlayer.addAll(Bukkit.getOnlinePlayers())

		val firstInput = input.first().lowercase()

		for (player in everyPlayer) {

			val playerNick = player.name?.lowercase() ?: continue
			if (playerNick.startsWith(firstInput)) {
				players.add(playerNick)
			}
		}
		return players
	}

	override fun wildcard(sender: CommandSender, vararg input: String): MutableList<Any?> {
		return Bukkit.getOfflinePlayers().toMutableList()
	}
}
