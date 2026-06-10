package red.man10.man10library.inventory.itemStack

import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import red.man10.man10library.MJavaPlugin
import red.man10.man10library.dslMarker.MItemStackDslMarker
import red.man10.man10library.utils.UnaryPlusBuilder

/**
 * ItemStack を DSL 形式で詳細に設定できるビルダークラス。
 *
 * 表示名、説明文（Lore）、ツールチップ非表示など、アイテムメタデータを容易に操作できます。
 * [IMItemStack] インターフェイスを実装し、テキスト形式の変換機能も提供します。
 *
 * ### 基本的な使用例
 *
 * ```kotlin
 * val item = MItemStack(Material.DIAMOND) {
 *     customNameMiniMessage = "<yellow><bold>Yellow Diamond"
 *     loreMiniMessage {
 *         + "<gray>Rare item"
 *         + "<gray>Worth lots of coins"
 *     }
 *     hideTooltip = true
 * }
 * ```
 *
 * ### プロパティの説明
 *
 * テキスト関連のプロパティは3つの形式で利用可能です：
 * - `customName` - Component 形式
 * - `customNameText` - Legacy 形式（色コード）
 * - `customNameMiniMessage` - MiniMessage 形式（タグベース）
 *
 * Lore（説明文）についても同様です：
 * - `lore` - 複数の Component のリスト
 * - `loreText` - Legacy 形式スト
 * - `loreMiniMessage` - MiniMessage 形式リスト
 *
 * @param itemStack 元になる ItemStack
 *
 * @see IMItemStack
 * @see MSimpleItemStack
 * @see MInventoryItem
 */
@Suppress("UnstableApiUsage", "unused")
@MItemStackDslMarker
open class MItemStack(val itemStack: ItemStack): IMItemStack {

    /**
     * ItemStack と初期化ラムダを受け取るコンストラクタ。
     *
     * @param itemStack 元になる ItemStack
     * @param init MItemStack の設定ラムダ
     */
    constructor(itemStack: ItemStack, init: MItemStack.() -> Unit): this(itemStack) {
        this.init()
    }

    /**
     * Material と初期化ラムダを受け取るコンストラクタ。
     *
     * @param material 元になる Material
     * @param init MItemStack の設定ラムダ
     */
    constructor(material: Material, init: MItemStack.() -> Unit): this(ItemStack(material), init)

    private fun <T> withMeta(action: (meta: ItemMeta) -> T): T? {
        val meta = itemStack.itemMeta ?: return null
        return action(meta)
    }

    /**
     * アイテムの表示名（Component 形式）。
     *
     * null を設定すると表示名がクリアされます。
     */
    var customName: Component?
        get() = withMeta { it.customName() }
        set(value) {
            itemStack.editMeta { it.customName(value) }
        }

    /**
     * アイテムの表示名（Legacy 形式 - 色コード `§x` 使用）。
     *
     * 内部的に [customName] と相互変換されます。
     *
     * @see customName
     * @see customNameMiniMessage
     */
    var customNameText: String?
        get() = customName?.let { toLegacy(it) }
        set(value) {
            customName = value?.let { fromLegacy(it) }
        }

    /**
     * アイテムの表示名（MiniMessage 形式 - タグベース）。
     *
     * 例：`"<yellow>My Item"`, `"<red><bold>Rare"`, `"<gradient:red:blue>Gradient"`
     *
     * 内部的に [customName] と相互変換されます。
     *
     * @see customName
     * @see customNameText
     */
    var customNameMiniMessage: String?
        get() = customName?.let { toMiniMessage(it) }
        set(value) {
            customName = value?.let { fromMiniMessage(it) }
        }

    /**
     * アイテムの説明文（Lore）- Component リスト形式。
     *
     * 複行のテキストを設定できます。各行が Lore の1行になります。
     */
    var lore: List<Component>?
        get() = withMeta { it.lore() }
        set(value) {
            itemStack.editMeta { it.lore(value) }
        }

    /**
     * アイテムの説明文（Lore）- Legacy 形式（色コード `§x` 使用）。
     *
     * 内部的に [lore] と相互変換されます。
     *
     * @see lore
     * @see loreMiniMessage
     */
    var loreText: List<String>?
        get() = lore?.map { toLegacy(it) }
        set(value) {
            lore = value?.map { fromLegacy(it) }
        }

    /**
     * アイテムの説明文（Lore）- MiniMessage 形式（タグベース）。
     *
     * 例：`listOf("<gray>説明1", "<gray>説明2")`
     *
     * 内部的に [lore] と相互変換されます。
     *
     * @see lore
     * @see loreText
     */
    var loreMiniMessage: List<String>?
        get() = lore?.map { toMiniMessage(it) }
        set(value) {
            lore = value?.map { fromMiniMessage(it) }
        }

    var customModelData: Float?
        get() = withMeta { meta ->
            if (!meta.hasCustomModelDataComponent()) return@withMeta null
            meta.customModelDataComponent.floats.firstOrNull()
        }
        set(value) {
            itemStack.editMeta { it.setCustomModelDataComponent(it.customModelDataComponent.apply {
                floats = listOfNotNull(value)
            }) }
        }

    var itemModel: Key?
        get() = itemStack.getData(DataComponentTypes.ITEM_MODEL)
        set(value) {
            if (value == null) {
                itemStack.unsetData(DataComponentTypes.ITEM_MODEL)
            } else {
                itemStack.setData(DataComponentTypes.ITEM_MODEL, value)
            }
        }

    /**
     * アイテムのツールチップ（ホバー時表示）を非表示にするかどうか。
     *
     * `true` にするとプレイヤーがアイテムをホバーしてもツールチップが表示されません。
     */
    var hideTooltip: Boolean
        get() = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY)?.hideTooltip() ?: false
        set(value) {
            setData(DataComponentTypes.TOOLTIP_DISPLAY) {
                TooltipDisplay.tooltipDisplay().apply {
                    hideTooltip(value)
                    val defaultData = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY)
                    if (defaultData != null) {
                        hiddenComponents(defaultData.hiddenComponents())
                    }
                }
            }
        }


    fun <T : Any> setData(componentType: DataComponentType.Valued<T>, value: T) {
        itemStack.setData(componentType, value)
    }

    fun <T: Any> setData(componentType: DataComponentType.Valued<T>, valueBuilder: (default: T?) -> DataComponentBuilder<T>) {
        val defaultValue = itemStack.getData(componentType)
        val newValue = valueBuilder(defaultValue).build()
        itemStack.setData(componentType, newValue)
    }

    fun <P: Any, C: Any> setPersistentData(key: NamespacedKey, type: PersistentDataType<P, C>, value: C) {
        itemStack.editMeta { meta ->
            meta.persistentDataContainer.set(key, type, value)
        }
    }

    fun <P: Any, C: Any> setPersistentData(key: String, type: PersistentDataType<P, C>, value: C) {
        val namespacedKey = NamespacedKey(MJavaPlugin.plugin, key)
        setPersistentData(namespacedKey, type, value)
    }

    /**
     * Lore（説明文）を Component ビルダーで設定します。
     *
     * [UnaryPlusBuilder] を使用して流暢な API で複数行の Lore を設定できます。
     *
     * @param builder Lore を構築するラムダ
     *
     * @see loreText
     * @see loreMiniMessage
     */
    fun lore(builder: UnaryPlusBuilder<Component>.() -> Unit) {
        val loreBuilder = UnaryPlusBuilder<Component>()
        loreBuilder.builder()
        lore = loreBuilder.build()
    }

    /**
     * Lore（説明文）を Legacy 形式で設定します。
     *
     * [UnaryPlusBuilder] を使用して流暢な API で複数行の Lore を設定できます。
     *
     * @param builder Legacy 形式のテキストで Lore を構築するラムダ
     *
     * @see lore
     * @see loreMiniMessage
     */
    fun loreText(builder: UnaryPlusBuilder<String>.() -> Unit) {
        val loreBuilder = UnaryPlusBuilder<String>()
        loreBuilder.builder()
        loreText = loreBuilder.build()
    }

    /**
     * Lore（説明文）を MiniMessage 形式で設定します。
     *
     * [UnaryPlusBuilder] を使用して流暢な API で複数行の Lore を設定できます。
     *
     * 例：
     * ```kotlin
     * loreMiniMessage {
     *     + "<gray>説明1"
     *     + "<red>注意</red>"
     * }
     * ```
     *
     * @param builder MiniMessage 形式のテキストで Lore を構築するラムダ
     *
     * @see lore
     * @see loreText
     */
    fun loreMiniMessage(builder: UnaryPlusBuilder<String>.() -> Unit) {
        val loreBuilder = UnaryPlusBuilder<String>()
        loreBuilder.builder()
        loreMiniMessage = loreBuilder.build()
    }

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