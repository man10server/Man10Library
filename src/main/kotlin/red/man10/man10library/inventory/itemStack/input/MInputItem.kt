package red.man10.man10library.inventory.itemStack.input

import org.bukkit.entity.Player
import red.man10.man10library.inventory.MInventory
import red.man10.man10library.inventory.itemStack.MInventoryItem

/**
 * 非 null の入力のみを受け付けるインベントリアイテム。
 *
 * [MNullableInputItem] をベースにしつつ、空入力や null を許可せず、
 * 指定した [type] に変換できた入力だけを [onEnter] に渡します。
 * 変換に失敗した場合は [MNullableInputItem.errorMessage] が送信され、入力は失敗として扱われます。
 *
 * このクラスは、必ず値が必要な設定項目や、数値・識別子などの厳密な入力検証を行いたい場合に適しています。
 *
 * ### 使用例
 *
 * ```kotlin
 * setInput(0, Material.STONE, Int::class.java) {
 *    message = Component.text("数値を入力してください")
 *    onEnter {
 *        player.sendMessage("入力された数値: $value")
 *    }
 *    onCancelled {
 *        player.sendMessage("入力がキャンセルされました")
 *    }
 * }
 * ```
 *
 * @param inventory この入力アイテムを配置している [MInventory]
 * @param mInventoryItem 元になる [MInventoryItem]
 * @param type 入力文字列を変換する対象の型
 *
 * @see MNullableInputItem
 */
@Suppress("unused")
class MInputItem<T: Any>(
    inventory: MInventory,
    mInventoryItem: MInventoryItem,
    type: Class<T>,
): MNullableInputItem<T>(inventory, mInventoryItem, type) {

    private var onEnter: (MInputContext<T>.() -> Unit)? = null

    /**
     * 入力成功時のコールバックを登録します。
     *
     * このクラスでは空入力は許可されないため、ここで受け取る [MInputContext.value] は必ず非 null です。
     *
     * @param listener 入力完了時に実行する処理
     */
    fun onEnter(listener: MInputContext<T>.() -> Unit) {
        onEnter = listener
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun onNullableEnter(listener: MNullableInputContext<T>.() -> Unit) {
        throw UnsupportedOperationException("MInputItem does not support onNullableEnter")
    }

    override fun createSession(player: Player, input: String): InputSession {
        return InputSession(
            onEnter = { msg, player ->
                val parsedInput = MInputItemManager.parseInput(msg, type)
                if (parsedInput == null) {
                    player.sendMessage(errorMessage(msg))
                    return@InputSession false
                }

                onEnter?.invoke(MInputContext(parsedInput, player))
                return@InputSession true
            },
            onCancelled = { player ->
                this.onCancelled?.invoke(player)
            }
        )
    }

    /**
     * [onEnter] に渡される入力結果コンテキスト。
     *
     * @param T 変換後の入力型
     * @property value 変換された入力値
     * @property player 入力したプレイヤー
     */
    data class MInputContext<T>(
        val value: T,
        val player: Player
    )
}