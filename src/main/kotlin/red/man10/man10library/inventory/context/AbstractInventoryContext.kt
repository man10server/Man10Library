package red.man10.man10library.inventory.context

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryEvent

/**
 * インベントリイベントの基底コンテキストクラス。
 *
 * インベントリのイベント処理（クリック、クローズなど）時に、
 * 関連情報を扱いやすく提供するためのクラスです。
 *
 * このクラスを直接継承することで、共通のプロパティ（イベント、プレイヤー）
 * にアクセスできます。
 *
 * @param inventoryEvent インベントリに関連する Bukkit イベント
 *
 * @see InventoryClickContext インベントリクリック時のコンテキスト
 * @see InventoryCloseContext インベントリクローズ時のコンテキスト
 */
abstract class AbstractInventoryContext(
    val inventoryEvent: InventoryEvent
) {
    /**
     * このイベントを発火させたプレイヤー。
     *
     * @return プレイヤーオブジェクト
     */
    val player
        get() = inventoryEvent.view.player as Player
}