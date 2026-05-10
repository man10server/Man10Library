package red.man10.man10library.command

import io.papermc.paper.command.brigadier.Commands

/**
 * コマンド定義をまとめる抽象ベースクラス。
 *
 * サブクラスはフィールドに `@MCommandBody` アノテーションを付与して `MCommandObject` を定義します。
 * コンストラクタで受け取った {@link Commands} レジストラを使用して、内部で定義されたコマンドを自動的に登録します。
 *
 * ### 最小限のサンプル
 *
 * ```kotlin
 * class HelloCommand(registrar: Commands) : MCommand(registrar) {
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
 * プラグイン側で `MJavaPlugin.registerCommands` に registrar を渡します：
 *
 * ```kotlin
 * class MyPlugin : MJavaPlugin() {
 *     override fun registerCommands(commands: Commands) {
 *         HelloCommand(commands)
 *     }
 * }
 * ```
 *
 * registrar の入手元について:
 * MCommand に渡す {@link Commands} の registrar は通常プラグインの `MJavaPlugin.registerCommands` で取得されます。
 * `MJavaPlugin.onEnable` にて Paper の LifecycleEvents.COMMANDS イベントから取得される `commands.registrar()` を渡してください。
 *
 * @param registrar Brigadier の Commands レジストラ（MJavaPlugin.registerCommands から取得したものを渡す）
 */
abstract class MCommand(registrar: Commands) {

    /** この MCommand に含まれるコマンド定義オブジェクトのリスト。 */
    val commands = mutableListOf<MCommandObject>()

    init {
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
     * 例:
     * val example by lazy { command { literal("hello") { executes { sender.sendMessage("hi") } } } }
     *
     * @param init MCommandObject を初期化するラムダ。トップレベルのリテラルや引数・実行ハンドラをここで定義します。
     * @return 生成された [MCommandObject]
     */
    protected fun command(init: MCommandObject.() -> Unit) = MCommandObject(init)
}