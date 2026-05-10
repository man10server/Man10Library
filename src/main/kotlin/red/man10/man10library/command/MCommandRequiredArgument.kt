package red.man10.man10library.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

/**
 * 必須引数（argument）を表すクラス。
 *
 * 名前と ArgumentType を持ち、補完候補（suggestions）やその他設定を受け取って Brigadier 用のビルダーを生成します。
 *
 * @param name 引数名
 * @param type 引数の型（Mojang Brigadier の ArgumentType）
 */
@Suppress("unused")
class MCommandRequiredArgument<T>(val name: String, val type: ArgumentType<T>, init: MCommandRequiredArgument<T>.() -> Unit): MCommandArgument() {

    /** 補完候補を返す関数型。実行コンテキストを受け取り、文字列リストを返す。 */
    typealias SuggestionProvider = MCommandData.() -> List<String>

    private val suggestions = mutableListOf<SuggestionProvider>()

    /**
     * 補完候補のプロバイダを登録する。
     * 複数登録した場合は全ての候補が結合されます。
     *
     * @param suggestionProvider 実行コンテキストを受け取り補完候補文字列のリストを返す関数
     */
    fun suggestions(suggestionProvider: SuggestionProvider) {
        suggestions.add(suggestionProvider)
    }

    init {
        init()
    }

    /**
     * Brigadier 用の ArgumentBuilder を構築して返す。
     *
     * @return Brigadier 用の [ArgumentBuilder]
     */
    override fun build(): ArgumentBuilder<CommandSourceStack, *> {
        val argumentBuilder = RequiredArgumentBuilder.argument<CommandSourceStack, T>(name, type)
        if (suggestions.isNotEmpty()) {
            argumentBuilder.suggests { context, builder ->
                val data = MCommandData(context)
                suggestions.forEach { suggestionProvider ->
                    suggestionProvider(data).forEach { suggestion ->
                        builder.suggest(suggestion)
                    }
                }
                builder.buildFuture()
            }
        }

        return argumentBuilder
    }
}