package red.man10.man10library

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

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

    /**
     * Brigadier の Commands レジストラを受け取り、コマンドを登録するためのフック。
     *
     * 注意: レジストラ取得は Paper のライフサイクルイベントから行われ、
     * MCommand 系クラスがこのレジストラを利用してコマンドを登録します。
     * 具体的には onEnable で LifecycleEvents.COMMANDS を購読し、
     * イベントオブジェクトの registrar() をこのメソッドに渡します。
     *
     * デフォルト実装は空です。プラグイン側でオーバーライドしてコマンド登録を行ってください。
     *
     * @param commands Brigadier の Commands レジストラ
     */
    open fun registerCommands(commands: Commands) {

    }

    override fun onLoad() {
        plugin = this
    }

    override fun onEnable() {
        // Paper のライフサイクルイベント(COMMANDS)を購読し、レジストラを registerCommands に渡す
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            registerCommands(commands.registrar())
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
