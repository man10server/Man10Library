package red.man10.man10library.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

/**
 * コマンド定義オブジェクト。
 *
 * 複数のトップレベル literal を保持し、それらを Brigadier の LiteralArgumentBuilder のリストとして生成します。
 */
@Suppress("unused")
class MCommandObject(init: MCommandObject.() -> Unit) {

    private val arguments = mutableListOf<MCommandLiteral>()

    init {
        init()
    }

    /**
     * トップレベルのリテラルを追加する。
     *
     * @param literal 固定語
     * @param init 初期化ラムダ
     */
    fun literal(literal: String, init: MCommandLiteral.() -> Unit) {
        val commandLiteral = MCommandLiteral(literal, init)
        arguments.add(commandLiteral)
    }

    /**
     * 定義済みのリテラルを Brigadier のビルダーリストとして返す。
     *
     * @return Brigadier 用の [LiteralArgumentBuilder] のリスト
     */
    fun build(): List<LiteralArgumentBuilder<CommandSourceStack>> {
        return arguments.map { it.build() }
    }
}