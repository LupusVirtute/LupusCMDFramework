package org.lupus.commands.core.managers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.lupus.commands.core.data.CommandLupi
import java.time.Instant

object CooldownManager {
    /**
     * Checks if player has cooldown on the given namespace
     */
    fun playerHasCooldown(player: Player, namespace: NamespacedKey): Boolean {
        val pdc = player.persistentDataContainer
        val has = pdc.has(namespace, PersistentDataType.LONG)
        if (!has)
            return false
        val timeStamp = pdc.get(namespace, PersistentDataType.LONG) ?: 0L
        val now = Instant.now()
        val timeStampInstant = Instant.ofEpochSecond(timeStamp)
        val isBefore = now.isBefore(timeStampInstant)
        if(!isBefore) {
            pdc.remove(namespace)
        }
        return isBefore
    }

    /**
     * Sets player cooldown given the namespace
     */
    fun setPlayerCooldown(player: Player, namespace: NamespacedKey, time: Instant) {
        val pdc = player.persistentDataContainer
        pdc.set(namespace, PersistentDataType.LONG, time.epochSecond)
    }
    /**
     * Gets the player cooldown on given namespace
     */
    fun getPlayerCooldown(player: Player, namespace: NamespacedKey): Instant {
        val pdc = player.persistentDataContainer
        return Instant.ofEpochSecond(pdc.getOrDefault(namespace, PersistentDataType.LONG, Instant.now().epochSecond))
    }
}