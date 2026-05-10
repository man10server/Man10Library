package red.man10.man10library.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

/**
 * 固定語（literal）を表す引数オブジェクト。
 *
 * @param literal コマンド内の固定語（例: "give", "teleport"）
 * @param init 初期化用のラムダ（子要素の追加や executes/permission の設定に使用）
 */
class MCommandLiteral(val literal: String, init: MCommandLiteral.() -> Unit): MCommandArgument() {

    init {
        init()
    }

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return LiteralArgumentBuilder.literal(literal)
    }
}