package red.man10.sampleproject.inventory

import org.bukkit.Material
import red.man10.man10library.inventory.builtin.MultiMInventory

class SampleMultiInventory: MultiMInventory(
    title = "Sample Multi Inventory",
    row = 3
) {

    override fun createSections() {
        section("default") {
            set(0, Material.DIAMOND) {
                customNameMiniMessage = "<yellow>Diamond"
                loreMiniMessage {
                    +"<gray>Rare item"
                }

                onClick {
                    switchSection("second")
                }
            }
            setInput(1, Material.PAPER, Int::class.java) {
                customNameMiniMessage = "<green>Input Paper"

                messageMiniMessage = "Please enter a number"
                errorMessageMiniMessage = { "<red>Please enter a valid number" }

                onEnter {
                    player.sendMessage("You entered: $value in the default section")
                }
            }
        }

        section("second") {
            set(0, Material.GOLD_INGOT) {
                customNameMiniMessage = "<yellow>Gold Ingot"
                loreMiniMessage {
                    +"<gray>Valuable item"
                }

                onClick {
                    switchSection("default")
                }
            }
            setInput(1, Material.PAPER, String::class.java) {
                customNameMiniMessage = "<green>Input Paper"

                messageMiniMessage = "Please enter some text"
                errorMessageMiniMessage = { "<red>Please enter valid text" }

                onEnter {
                    player.sendMessage("You entered: $value in the second section")
                }
            }
        }
    }


}