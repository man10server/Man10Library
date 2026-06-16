package red.man10.sampleproject

import net.kyori.adventure.key.Key
import org.bukkit.Material
import red.man10.man10library.inventory.MInventory

class SampleInventory: MInventory("Sample Inventory", 6) {

    override fun renderContents() {
        set(0, Material.DIAMOND) {
            customNameMiniMessage = "<yellow>Diamond"
            loreMiniMessage {
                +"<gray>Rare item"
            }

            itemModel = Key.key("minecraft", "diamond_sword")

            onClick {
                // this は InventoryClickContext
                player.sendMessage("You clicked the diamond!")
                // event から詳細情報を取得
                println("Click type: ${inventoryClickEvent.click}")
            }
        }

        setInput(1, Material.PAPER, Int::class.java) {
            customNameMiniMessage = "<green>Input Paper"

            messageMiniMessage = "Please enter a number"
            errorMessageMiniMessage = { "<red>Please enter a valid number" }

            onEnter {
                player.sendMessage("You entered: $value")
            }
        }
    }
}