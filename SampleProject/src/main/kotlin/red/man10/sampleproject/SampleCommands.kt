package red.man10.sampleproject

import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.entity.Player
import red.man10.man10library.command.MCommand
import red.man10.man10library.command.MCommandBody

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
                executes {
                    val player = (context.source.executor ?: context.source.sender) as? Player
                    if (player != null) {
                        SampleInventory().open(player)
                    } else {
                        sender.sendPlainMessage("This command can only be used by a player.")
                    }
                    0
                }
            }
        }
    }
}