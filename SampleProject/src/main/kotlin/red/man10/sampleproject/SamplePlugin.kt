package red.man10.sampleproject

import io.papermc.paper.command.brigadier.Commands
import red.man10.man10library.MJavaPlugin

class SamplePlugin: MJavaPlugin() {

    override fun registerCommands(commands: Commands) {
        SampleCommands(commands)
    }
}