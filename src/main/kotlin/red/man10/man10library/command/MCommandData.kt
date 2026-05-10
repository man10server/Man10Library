package red.man10.man10library.command

import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender

/**
 * Brigadier の CommandContext をラップするデータオブジェクト。
 *
 * コマンド実行ハンドラ内ではこのクラスをレシーバーとして利用し、
 * コンテキスト情報（送信者や引数）を簡便に取得できます。
 *
 * @property context Brigadier の CommandContext
 */
@Suppress("unused")
open class MCommandData(
    val context: CommandContext<CommandSourceStack>
) {

    /** コマンド実行者（Bukkit の CommandSender） */
    val sender: CommandSender
        get() = context.source.sender

    /**
     * Brigadier から引数を取得するユーティリティ。
     *
     * @param name 引数名
     * @param clazz 期待する型の Class
     * @return 引数の値（指定した型にキャストされる）
     */
    fun <T> getArgument(name: String, clazz: Class<T>): T {
        return context.getArgument(name, clazz)
    }
}