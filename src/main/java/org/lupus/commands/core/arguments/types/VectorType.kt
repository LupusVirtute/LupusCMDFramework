package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.lupus.commands.core.arguments.ArgumentType
import java.util.*

object VectorType : ArgumentType(Vector::class.java, 3, "x,y,z") {
	override fun conversion(sender: CommandSender, vararg input: String): Any {
		if (sender is Player) {
			val loc = sender.location
			val array = arrayOf("%.2f".format(loc.x) + "", "%.2f".format(loc.y), "%.2f".format(loc.z))
			for ((i, _) in array.withIndex()) {
				if (input[i] == "~")
					System.arraycopy(array, i, input, i, 1)
			}
		}
		return Vector(input[0].toDouble(), input[1].toDouble(), input[2].toDouble())
	}

	override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
		val coords: MutableList<String> = ArrayList()
		if (sender is Player) {
			val playerLoc = sender.location
			coords.add("%.2f".format(playerLoc.x) + " %.2f".format(playerLoc.y) + " %.2f".format(playerLoc.z))
		}
		return coords
	}
}