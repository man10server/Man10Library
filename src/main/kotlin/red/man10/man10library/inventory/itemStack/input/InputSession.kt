package red.man10.man10library.inventory.itemStack.input

import org.bukkit.entity.Player

data class InputSession(
    val onEnter: (String, Player) -> Boolean,
    val onCancelled: (Player) -> Unit
)