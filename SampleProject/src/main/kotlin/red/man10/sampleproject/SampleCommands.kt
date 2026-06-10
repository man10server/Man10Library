package red.man10.sampleproject

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import red.man10.man10library.command.MCommand
import red.man10.man10library.command.MCommandBody

class SampleCommands(registrar: Commands): MCommand(registrar) {

    @MCommandBody
    val sampleCommands = command {
        literal("sample") {
            literal("hello") {
                argument("target", ArgumentTypes.player()) {
                    executes {
                        val target = context.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                            .resolve(context.source)
                            .first()

                        target.sendMessage("Hello from ${context.source.sender.name}!")
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
                        context.source.sender.sendMessage("This command can only be used by a player.")
                    }
                    0
                }
            }
        }
    }
}