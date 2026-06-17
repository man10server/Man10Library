package red.man10.man10library

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10library.command.MCommand

/**
 * ベースとなる JavaPlugin 拡張クラス。
 *
 * サブクラスはプラグイン固有の初期化処理やコマンド登録を行います。
 */
@Suppress("unused")
abstract class MJavaPlugin : JavaPlugin() {

    companion object {
        /** 現在のプラグインインスタンス。static 相当としてアクセス可能にするためのフィールド。 */
        lateinit var plugin: MJavaPlugin
    }

    override fun onLoad() {
        plugin = this
    }

    abstract fun onPluginEnabled()

    @Deprecated("Use onPluginEnabled() instead", ReplaceWith("onPluginEnabled()"), DeprecationLevel.ERROR)
    override fun onEnable() {
        onPluginEnabled()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun registerCommands(vararg commands: MCommand) {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { reloadableRegistrarEvent ->
            commands.forEach { command ->
                command.register(reloadableRegistrarEvent.registrar())
            }
        }
    }
}
