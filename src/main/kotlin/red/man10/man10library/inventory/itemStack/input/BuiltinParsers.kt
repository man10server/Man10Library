package red.man10.man10library.inventory.itemStack.input

import org.bukkit.Bukkit
import org.bukkit.Location

object StringParser: IInputParser<String> {
    override fun parse(input: String): String {
        return input
    }
}

object IntParser: IInputParser<Int> {
    override fun parse(input: String): Int? {
        return input.toIntOrNull()
    }
}

object DoubleParser: IInputParser<Double> {
    override fun parse(input: String): Double? {
        return input.toDoubleOrNull()
    }
}

object FloatParser: IInputParser<Float> {
    override fun parse(input: String): Float? {
        return input.toFloatOrNull()
    }
}

object LongParser: IInputParser<Long> {
    override fun parse(input: String): Long? {
        return input.toLongOrNull()
    }
}

object BooleanParser: IInputParser<Boolean> {
    override fun parse(input: String): Boolean? {
        return input.toBooleanStrictOrNull()
    }
}

object LocationParser: IInputParser<Location> {
    override fun parse(input: String): Location? {
        val parts = input.split(" ").map { it.trim() }
        when (parts.size) {
            3 -> {
                val x = parts[0].toDoubleOrNull() ?: return null
                val y = parts[1].toDoubleOrNull() ?: return null
                val z = parts[2].toDoubleOrNull() ?: return null
                return Location(null, x, y, z)
            }
            4 -> {
                val world = Bukkit.getWorld(parts[0]) ?: return null
                val x = parts[1].toDoubleOrNull() ?: return null
                val y = parts[2].toDoubleOrNull() ?: return null
                val z = parts[3].toDoubleOrNull() ?: return null
                return Location(world, x, y, z)
            }
            6 -> {
                val world = Bukkit.getWorld(parts[0]) ?: return null
                val x = parts[1].toDoubleOrNull() ?: return null
                val y = parts[2].toDoubleOrNull() ?: return null
                val z = parts[3].toDoubleOrNull() ?: return null
                val yaw = parts[4].toFloatOrNull() ?: return null
                val pitch = parts[5].toFloatOrNull() ?: return null
                return Location(world, x, y, z, yaw, pitch)
            }
            else -> return null
        }
    }
}