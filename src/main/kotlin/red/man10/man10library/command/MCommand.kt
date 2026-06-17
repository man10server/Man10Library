package red.man10.man10library.command

import io.papermc.paper.command.brigadier.Commands

/**
 * コマンド定義をまとめる抽象ベースクラス。
 *
 * サブクラスはフィールドに `@MCommandBody` アノテーションを付与して `MCommandObject` を定義します。
 * `MJavaPlugin.registerCommands` を使用して、内部で定義されたコマンドを自動的に登録します。
 *
 * ### 最小限のサンプル
 *
 * ```kotlin
 * class HelloCommand : MCommand() {
 *     @MCommandBody
 *     val hello = command {
 *         literal("hello") {
 *             executes {
 *                 sender.sendMessage("Hello!")
 *                 return@executes 0
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * プラグイン側で `MJavaPlugin.registerCommands`で登録します
 *
 * ```kotlin
 * class MyPlugin : MJavaPlugin() {
 *    override fun onPluginEnabled() {
 *       registerCommands(HelloCommand())
 *    }
 * }
 * ```
 */
@Suppress("unused")
abstract class MCommand {

    /** この MCommand に含まれるコマンド定義オブジェクトのリスト。 */
    private val commands = mutableListOf<MCommandObject>()

    internal fun register(registrar: Commands) {
        // リフレクションで @MCommandBody が付いたフィールドを探し、MCommandObject を収集する
        commands.clear()
        javaClass.declaredFields.forEach { field ->
            if (field.isAnnotationPresent(MCommandBody::class.java)) {
                field.isAccessible = true
                val commandObject = field.get(this) as? MCommandObject ?: return@forEach
                commands.add(commandObject)
            }
        }

        // 収集した各 MCommandObject から Brigadier 用のビルダーを作り、registrar に登録する
        commands.forEach { command ->
            command.build().forEach {
                registrar.register(it.build())
            }
        }
    }

    /**
     * コマンド定義を作るためのユーティリティ。
     *
     * サブクラス内で `@MCommandBody` を付与したプロパティの初期化に使用します。
     *
     * @param init MCommandObject を初期化するラムダ。トップレベルのリテラルや引数・実行ハンドラをここで定義します。
     * @return 生成された [MCommandObject]
     */
    protected fun command(init: MCommandObject.() -> Unit) = MCommandObject(init)
}