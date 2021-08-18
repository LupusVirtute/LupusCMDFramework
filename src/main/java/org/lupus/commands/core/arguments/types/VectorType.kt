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
			val array = arrayOf(loc.x.toString() + "", loc.y.toString() + "", loc.z.toString() + "")
			System.arraycopy(array, 0, input, 0, 3)
		}
		return Vector(input[0].toInt(), input[1].toInt(), input[2].toInt())
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