package red.man10.sampleproject

import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.entity.Player
import red.man10.man10library.command.MCommand
import red.man10.man10library.command.MCommandBody
import red.man10.sampleproject.inventory.SampleInventory
import red.man10.sampleproject.inventory.SampleMultiInventory

class SampleCommands: MCommand() {

    @MCommandBody
    val sampleCommands = command {
        literal("sample") {
            literal("hello") {
                argument("target", ArgumentTypes.player()) {
                    executes {
                        val target = getPlayer("target")

                        target.sendPlainMessage("Hello from ${sender.name}!")
                        return@executes 0
                    }
                }
            }

            literal("inventory") {
                literal("normal") {
                    executes {
                        val player = sender as? Player ?: return@executes 0

                        SampleInventory().open(player)
                        return@executes 0
                    }
                }

                literal("multi") {
                    executes {
                        val player = sender as? Player ?: return@executes 0

                        SampleMultiInventory().open(player)
                        return@executes 0
                    }
                }
            }
        }
    }
}