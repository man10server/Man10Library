package red.man10.man10library.command

import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

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

    /**
     * Brigadier から引数を取得するユーティリティ。
     *
     * @param name 引数名
     * @return 引数の値（指定した型にキャストされる）
     */
    inline fun <reified T> getArgument(name: String): T {
        return getArgument(name, T::class.java)
    }

    /**
     * 引数名からエンティティのリストを取得する。
     *
     * @param name 引数名
     * @return 選択されたエンティティのリスト
     */
    fun getEntities(name: String): List<Entity> {
        val argument = getArgument(name, EntitySelectorArgumentResolver::class.java)
        return argument.resolve(context.source)
    }

    /**
     * 引数名から最初のエンティティを取得する。
     *
     * @param name 引数名
     * @return 選択された最初のエンティティ
     */
    fun getEntity(name: String): Entity {
        val entities = getEntities(name)
        return entities.first()
    }

    /**
     * 引数名からプレイヤーのリストを取得する。
     *
     * @param name 引数名
     * @return 選択されたプレイヤーのリスト
     */
    fun getPlayers(name: String): List<Player> {
        val argument = getArgument(name, PlayerSelectorArgumentResolver::class.java)
        return argument.resolve(context.source)
    }

    /**
     * 引数名から最初のプレイヤーを取得する。
     *
     * @param name 引数名
     * @return 選択された最初のプレイヤー
     */
    fun getPlayer(name: String): Player {
        val players = getPlayers(name)
        return players.first()
    }
}