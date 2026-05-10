package red.man10.man10library.inventory.context

import org.bukkit.event.inventory.InventoryClickEvent
import red.man10.man10library.inventory.itemStack.MInventoryItem

/**
 * インベントリ内のアイテムをクリックした時のコンテキスト。
 *
 * [MInventoryItem.onClick] ラムダ内で `this` として利用可能です。
 *
 * ### 使用例
 *
 * ```kotlin
 * set(0, Material.DIAMOND) {
 *     onClick {
 *         // this は InventoryClickContext
 *         player.sendMessage("You clicked on Diamond!")
 *         // event から詳細情報を取得
 *         println("Click type: ${inventoryClickEvent.click}")
 *     }
 * }
 * ```
 *
 * @param inventoryClickEvent クリックイベント（Bukkit）
 * @param mInventoryItem クリックされたアイテム
 *
 * @see MInventoryItem.onClick
 * @see AbstractInventoryContext
 */
@Suppress("unused")
class InventoryClickContext(
    /** クリックイベント。詳細情報（クリック種別など）を取得できます。 */
    val inventoryClickEvent: InventoryClickEvent,
    /** クリックされたアイテム。アイテムの情報を取得できます。 */
    val mInventoryItem: MInventoryItem
): AbstractInventoryContext(inventoryClickEvent)