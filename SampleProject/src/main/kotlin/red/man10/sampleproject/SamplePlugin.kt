package red.man10.sampleproject

import red.man10.man10library.MJavaPlugin

class SamplePlugin: MJavaPlugin() {

    override fun onPluginEnabled() {
        registerCommands(SampleCommands())
    }
}