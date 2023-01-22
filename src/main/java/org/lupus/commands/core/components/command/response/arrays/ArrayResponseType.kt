package org.lupus.commands.core.components.command.response.arrays

import net.kyori.adventure.text.Component
import org.lupus.commands.core.data.CommandLupi

interface ArrayResponseType {
	val command: CommandLupi
	fun check(array: Array<*>): Boolean
	fun componentResponse(array: Array<*>): Component
}
