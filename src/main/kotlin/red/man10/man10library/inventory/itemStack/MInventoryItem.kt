package red.man10.man10library.inventory.itemStack

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import red.man10.man10library.dslMarker.MItemStackDslMarker
import red.man10.man10library.inventory.context.InventoryClickContext

/**
 * インベントリに配置されるアイテムクラス。
 *
 * [MItemStack] を継承しており、[MItemStack] のすべての機能（表示名、Lore、テキスト形式変換）
 * に加えて、クリックイベントハンドリング機能を提供します。
 *
 * [MInventory] の `set` メソッドで使用され、アイテムクリック時に登録されたコールバック
 * が自動的に呼び出されます。
 *
 * ### 使用例
 *
 * ```kotlin
 * val item = MInventoryItem(Material.DIAMOND) {
 *     customNameMiniMessage = "<yellow>Diamond"
 *     loreMiniMessage {
 *         + "<gray>Rare item"
 *     }
 *
 *     onClick {
 *         // this は InventoryClickContext
 *         player.sendMessage("You clicked the diamond!")
 *     }
 * }
 *
 * inventory.set(0, item)
 * ```
 *
 * または DSL でインベントリに直接配置：
 *
 * ```kotlin
 * inventory.set(0, Material.DIAMOND) {
 *     customNameMiniMessage = "<yellow>Diamond"
 *     onClick {
 *         player.sendMessage("Clicked!")
 *     }
 * }
 * ```
 *
 * @param itemStack 元になる ItemStack
 *
 * @see MItemStack
 * @see InventoryClickContext
 * @see MInventory.set
 */
@MItemStackDslMarker
class MInventoryItem(itemStack: ItemStack): MItemStack(itemStack) {

    /**
     * ItemStack と初期化ラムダを受け取るコンストラクタ。
     *
     * @param itemStack 元になる ItemStack
     * @param init MInventoryItem の設定ラムダ
     */
    constructor(itemStack: ItemStack, init: MInventoryItem.() -> Unit): this(itemStack) {
        init()
    }

    /**
     * Material と初期化ラムダを受け取るコンストラクタ。
     *
     * デフォルト値として空のラムダが設定されているため、初期化なしで使用することも可能です。
     *
     * @param material 元になる Material
     * @param init MInventoryItem の設定ラムダ（デフォルト：空）
     */
    constructor(material: Material, init: MInventoryItem.() -> Unit = {}): this(ItemStack(material)) {
        init()
    }

    /** クリック時に実行されるコールバックのリスト。 */
    private val onClick: MutableList<InventoryClickContext.() -> Unit> = mutableListOf()

    /**
     * アイテムクリック時のコールバックを登録します。
     *
     * 複数のコールバックを登録することができ、すべてが実行されます。
     *
     * @param action クリック時実行のラムダ（`this` は [InventoryClickContext]）
     *
     * @see handleClick
     */
    fun onClick(action: InventoryClickContext.() -> Unit) {
        onClick.add(action)
    }

    /**
     * クリックイベントを処理します。（内部用）
     *
     * 登録されたすべてのコールバックを実行します。
     *
     * @param context クリックイベントのコンテキスト
     */
    internal fun handleClick(context: InventoryClickContext) {
        onClick.forEach { it(context) }
    }
}