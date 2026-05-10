package red.man10.man10library.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import red.man10.man10library.command.MCommandData
import red.man10.man10library.dslMarker.MCommandDslMarker

/**
 * コマンドの引数やリテラルの基底クラス。
 *
 * ビルダーを構築するための抽象メソッド [build] を提供し、
 * 実行ハンドラ（executes）や権限/要件（requires）を組み合わせて最終的な Brigadier のビルダーを生成します。
 *
 * このクラスはライブラリの利用者（プラグイン作成者）向けに設計されています。主に下記のメソッドを使って
 * コマンドの構造を DSL 的に記述します。
 *
 * - [literal]: 固定語（サブコマンドなど）を追加します。
 * - [argument]: 引数（必須）を追加します。
 * - [executes]: コマンド実行時の処理を登録します。
 * - [requires]: 実行要件（権限など）を追加します。
 */
@Suppress("unused")
@MCommandDslMarker
abstract class MCommandArgument {
    /** コマンド送信者に対する実行許可判定を行う関数型 */
    typealias Requirement = CommandSender.() -> Boolean
    /** コマンド実行時に呼ばれる関数。戻り値は Brigadier の結果コード（通常 0 など） */
    typealias Executor = MCommandData.() -> Int

    private val children = mutableListOf<MCommandArgument>()
    private val requires = mutableListOf<Requirement>()
    private var executor: Executor? = null

    /**
     * 実際の Brigadier 用 ArgumentBuilder を構築して返す。サブクラスで実装する。
     *
     * @return Brigadier 用の [ArgumentBuilder]
     */
    abstract fun build(): ArgumentBuilder<CommandSourceStack, *>

    /**
     * リテラル（固定語）を子要素として追加するユーティリティ。
     *
     * @param literal コマンド内の固定語（例: "give", "teleport", "subcmd"）
     * @param init 追加したリテラルに対する初期化ラムダ。子要素（argument や executes など）をここで定義します。
     */
    fun literal(literal: String, init: MCommandLiteral.() -> Unit = {}) {
        val argument = MCommandLiteral(literal, init)
        children.add(argument)
    }

    /**
     * 必須引数を子要素として追加するユーティリティ。
     *
     * @param name 引数名（例: "player", "amount"）。ビルド時に Brigadier の引数名として使用されます。
     * @param type Mojang Brigadier の [ArgumentType]（例: StringArgumentType.word(), IntegerArgumentType.integer() など）
     * @param init 引数に対する初期化ラムダ。補完（suggestions）や実行処理をここで定義します。
     */
    fun <T> argument(name: String, type: ArgumentType<T>, init: MCommandRequiredArgument<T>.() -> Unit = {}) {
        val argument = MCommandRequiredArgument(name, type, init)
        children.add(argument)
    }

    /**
     * コマンド実行時の処理を登録する。
     *
     * @param executor コマンド実行時に呼ばれるラムダ。レシーバーは [MCommandData] で、
     * レシーバー内から sender や getArgument を利用できます。戻り値は Brigadier の終了コード（通常 0）。
     */
    fun executes(executor: Executor) {
        this.executor = executor
    }

    /**
     * 追加の実行要件（ラムダ）を登録する。
     *
     * @param requirement コマンドを実行可能か判定するラムダ。引数は Bukkit の [CommandSender]。
     * すべての要件が true を返すと実行が許可されます。
     */
    fun requires(requirement: Requirement) {
        requires.add(requirement)
    }

    /**
     * 指定されたパーミッションを持つことを実行要件に追加するショートカット。
     *
     * @param permission 必要なパーミッションノード（例: "myplugin.command.use"）
     */
    fun permission(permission: String) {
        requires { hasPermission(permission) }
    }

    /**
     * このオブジェクトと子要素の情報を元に Brigadier 用の ArgumentBuilder を組み立てる。
     * 内部で executor / requires / children を結合して返却する。
     *
     * @return Brigadier 用の [ArgumentBuilder]
     */
    internal fun buildForBrigadier(): ArgumentBuilder<CommandSourceStack, *> {
        val builder = build()

        executor?.let { executor ->
            builder.executes { context ->
                val data = MCommandData(context)
                executor(data)
            }
        }

        if (requires.isNotEmpty()) {
            builder.requires { source ->
                val sender = source.sender
                requires.all { requirement -> requirement(sender) }
            }
        }

        children.forEach { child ->
            builder.then(child.buildForBrigadier())
        }
        return builder
    }
}