package red.man10.man10library.inventory.context

import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * インベントリを閉じた時のコンテキスト。
 *
 * [red.man10.man10library.inventory.MInventory.onClose] リスト内のラムダで `this` として利用可能です。
 *
 * ### 使用例
 *
 * ```kotlin
 * inventory.onClose {
 *     // this は InventoryCloseContext
 *     player.sendMessage("You closed the inventory!")
 * }
 * ```
 *
 * @param inventoryCloseEvent クローズイベント（Bukkit）
 *
 * @see red.man10.man10library.inventory.MInventory.onClose
 * @see AbstractInventoryContext
 */
@Suppress("unused")
class InventoryCloseContext(
    /** クローズイベント。詳細情報を取得できます。 */
    val inventoryCloseEvent: InventoryCloseEvent
): AbstractInventoryContext(inventoryCloseEvent)