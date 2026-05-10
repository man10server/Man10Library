package red.man10.man10library.inventory.builtin

import net.kyori.adventure.text.Component
import org.bukkit.Material
import red.man10.man10library.inventory.MInventory
import red.man10.man10library.inventory.itemStack.MInventoryItem

/**
 * ページング機能を備えた大規模インベントリ。
 *
 * 大量のアイテムをページ分割して表示できます。デフォルトでは 6 行（54 スロット）の
 * インベントリが作成され、最下段（45～53）がナビゲーションエリアになります。
 *
 * ページ当たり 45 個のアイテムを表示でき、前へ・次へボタンでページを切り替えられます。
 *
 * ### 使用例
 *
 * ```kotlin
 * class PlayerListInventory : LargeMInventory("Players") {
 *     override val resources: () -> List<MInventoryItem> = {
 *         // プレイヤーのリストを返す
 *         Bukkit.getOnlinePlayers().map { player ->
 *             MInventoryItem(Material.PLAYER_HEAD) {
 *                 customNameMiniMessage = "<yellow>${player.name}"
 *                 onClick {
 *                     player.sendMessage("Clicked!")
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * val inventory = PlayerListInventory()
 * inventory.open(player)
 * ```
 *
 * ### カスタマイズ
 *
 * `backgroundItem`、`previousPageItem`、`nextPageItem` をオーバーライドして、
 * ナビゲーション UI をカスタマイズできます。
 *
 * ```kotlin
 * class MyLargeInventory(title: String) : LargeMInventory(title) {
 *     override var backgroundItem = MInventoryItem(Material.BLACK_STAINED_GLASS_PANE) {
 *         hideTooltip = true
 *     }
 *
 *     override val resources = {
 *         // アイテム一覧を返す
 *     }
 * }
 * ```
 *
 * @param title インベントリの表示名（[Component] 形式）
 *
 * @see MInventory
 * @see MInventoryItem
 */
@Suppress("unused")
abstract class LargeMInventory(title: Component): MInventory(title, 6) {
    /**
     * String タイトルを指定する別のコンストラクタ。
     *
     * @param title インベントリの表示名
     */
    constructor(title: String): this(Component.text(title))

    /** 現在表示されているページ番号（0 ベース）。 */
    var currentPage = 0

    /**
     * ナビゲーション領域の背景アイテム。
     *
     * デフォルトではシアンのガラスパネルです。
     * オーバーライドしてカスタマイズできます。
     */
    open var backgroundItem: MInventoryItem = MInventoryItem(Material.CYAN_STAINED_GLASS_PANE) {
        hideTooltip = true
    }

    /**
     * 前ページへ移動するボタン。
     *
     * デフォルトでは赤いガラスパネルで「前へ」と表示されます。
     * ページ 0 では自動的に非表示になります。
     * オーバーライドしてカスタマイズできます。
     */
    open var previousPageItem: MInventoryItem = MInventoryItem(Material.RED_STAINED_GLASS_PANE) {
        customNameMiniMessage = "<red><bold>前へ"

        onClick {
            currentPage--
            render()
        }
    }

    /**
     * 次ページへ移動するボタン。
     *
     * デフォルトでは緑のガラスパネルで「次へ」と表示されます。
     * 最後のページでは自動的に非表示になります。
     * オーバーライドしてカスタマイズできます。
     */
    open var nextPageItem: MInventoryItem = MInventoryItem(Material.LIME_STAINED_GLASS_PANE) {
        customNameMiniMessage = "<green><bold>次へ"

        onClick {
            currentPage++
            render()
        }
    }

    /**
     * 表示するアイテム一覧を返すラムダ。
     *
     * この関数は [renderContents] から呼び出され、毎回最新のアイテム情報を取得します。
     * ページング処理は自動的に行われるため、ここではすべてのアイテムを返してください。
     *
     * @return 表示対象のすべての [MInventoryItem] のリスト
     */
    abstract val resources: () -> List<MInventoryItem>

    /**
     * インベントリのコンテンツをレンダリングします。
     *
     * 以下の処理を行います：
     * 1. [resources] からアイテムリストを取得
     * 2. ナビゲーション領域を背景アイテムで埋める
     * 3. ページ遷移ボタンの表示/非表示を判定
     * 4. 現在ページのアイテムを配置（1ページ当たり 45 個）
     *
     * @see renderOnSet
     * @see MInventory.render
     */
    override fun renderContents() {
        val items = resources()

        // ナビゲーション領域を背景で埋める（45～53）
        set(45..53, backgroundItem)

        // ページネーション判定
        val hasPreviousPage = currentPage > 0
        val hasNextPage = items.size > (currentPage + 1) * 45

        // ボタン配置
        if (hasPreviousPage) set(45, previousPageItem)
        if (hasNextPage) set(53, nextPageItem)

        // 現在ページのアイテムを配置（最大 45 個）
        items.drop(currentPage * 45).take(45).forEachIndexed { index, item ->
            set(index, item)
        }
    }
}