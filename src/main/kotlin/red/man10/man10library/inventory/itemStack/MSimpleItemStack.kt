package red.man10.man10library.inventory.itemStack

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * ItemStack の表示名と Lore を簡潔に操作するビルダークラス。
 *
 * [MItemStack] よりもシンプルなインターフェイスを提供し、
 * メソッドチェーン対応で直感的に使用できます。
 *
 * **推奨される使用場面:**
 * - 表示名と Lore だけを設定したい場合
 * - メソッドチェーンで流暢に設定したい場合
 *
 * **より詳細な設定が必要な場合は [MItemStack] を使用してください。**
 *
 * ### 使用例
 *
 * ```kotlin
 * val item = MSimpleItemStack(Material.DIAMOND)
 *     .setDisplayName("<yellow>Diamond")
 *     .setLore("<gray>Rare item", "<gray>Worth coins")
 * ```
 *
 * @param itemStack 元になる ItemStack
 *
 * @see IMItemStack
 * @see MItemStack
 * @see MInventoryItem
 */
@Suppress("unused")
class MSimpleItemStack(val itemStack: ItemStack): IMItemStack {

    /**
     * Material から ItemStack を生成するコンストラクタ。
     *
     * @param material 元になる Material
     */
    constructor(material: Material): this(ItemStack(material))

    /**
     * ItemMeta を取得または作成します。（内部用）
     *
     * @return ItemMeta オブジェクト
     */
    private fun getItemMeta(): ItemMeta {
        return if (itemStack.hasItemMeta()) {
            itemStack.itemMeta
        } else {
            Bukkit.getItemFactory().getItemMeta(itemStack.type)
        }
    }

    /**
     * アイテムの表示名を取得します。
     *
     * @return 表示名（Legacy 形式）。表示名がない場合は空文字列
     */
    fun getDisplayName(): String {
        val component = getItemMeta().customName() ?: return ""
        return toLegacy(component)
    }

    /**
     * アイテムの表示名を設定します。
     *
     * @param displayName 表示名（Legacy 形式）
     * @return メソッドチェーン用に this を返す
     *
     * @see getDisplayName
     */
    fun setDisplayName(displayName: String): MSimpleItemStack {
        itemStack.editMeta { meta ->
            meta.customName(fromLegacy(displayName))
        }
        return this
    }

    /**
     * アイテムの説明文（Lore）を取得します。
     *
     * @return Lore のリスト（Legacy 形式）。Lore がない場合は空リスト
     */
    fun getLore(): List<String> {
        val lore = getItemMeta().lore() ?: return emptyList()
        return lore.map { toLegacy(it) }
    }

    /**
     * アイテムの説明文（Lore）を設定します。
     *
     * @param lore 設定する Lore のリスト（Legacy 形式）
     * @return メソッドチェーン用に this を返す
     *
     * @see getLore
     * @see addLore
     */
    fun setLore(lore: List<String>): MSimpleItemStack {
        itemStack.editMeta { meta ->
            meta.lore(lore.map { fromLegacy(it) })
        }
        return this
    }

    /**
     * アイテムの説明文（Lore）を可変長引数で設定します。
     *
     * @param lore 設定する Lore（可変長引数）
     * @return メソッドチェーン用に this を返す
     *
     * @see setLore
     */
    fun setLore(vararg lore: String) = setLore(lore.toList())

    /**
     * アイテムの説明文（Lore）に行を追加します。
     *
     * 既存の Lore に追加される形になります。
     *
     * @param lore 追加する Lore のリスト（Legacy 形式）
     * @return メソッドチェーン用に this を返す
     *
     * @see setLore
     * @see getLore
     */
    fun addLore(lore: List<String>): MSimpleItemStack {
        itemStack.editMeta { meta ->
            val currentLore = meta.lore() ?: emptyList()
            meta.lore(currentLore + lore.map { fromLegacy(it) })
        }
        return this
    }

    /**
     * アイテムの説明文（Lore）に行を可変長引数で追加します。
     *
     * 既存の Lore に追加される形になります。
     *
     * @param lore 追加する Lore（可変長引数）
     * @return メソッドチェーン用に this を返す
     *
     * @see addLore
     */
    fun addLore(vararg lore: String) = addLore(lore.toList())


    /**
     * このアイテムを Bukkit の ItemStack に変換して返します。
     *
     * 返されたアイテムスタックは設定した内容の複製です。
     *
     * @return 生成した ItemStack（複製）
     */
    override fun build(): ItemStack {
        return itemStack.clone()
    }
}