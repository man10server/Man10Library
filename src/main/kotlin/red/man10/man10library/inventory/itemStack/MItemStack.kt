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
import red.man10.man10library.utils.byLegacy
import red.man10.man10library.utils.byMiniMessage
import red.man10.man10library.utils.toLegacy
import red.man10.man10library.utils.toMiniMessage

/**
 * ItemStack を DSL 形式で詳細に設定できるビルダークラス。
 *
 * Bukkit / Paper の ItemMeta と Data Component をまとめて扱い、
 * 表示名、Lore、custom model data、item model、ツールチップ表示、PersistentData などを
 * ひとつの API で設定できます。
 * [IMItemStack] を実装しているため、Legacy / MiniMessage / Component の相互変換も利用できます。
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
 *     customModelData = 1001f
 *     itemModel = Key.key("example", "diamond")
 *     hideTooltip = true
 * }
 * ```
 *
 * ### 主な設定項目
 *
 * - `customName` / `customNameText` / `customNameMiniMessage` - 表示名の設定
 * - `lore` / `loreText` / `loreMiniMessage` - 説明文の設定
 * - `customModelData` - カスタムモデル用の数値データ
 * - `itemModel` - Paper の item model データコンポーネント
 * - `hideTooltip` - ホバー時のツールチップ表示制御
 * - `setPersistentData` - 追加情報を PDC に保存
 *
 * ### 注意点
 *
 * `build()` で返される ItemStack は内部保持しているスタックの複製です。
 * そのため、返却後の変更はこのビルダー本体には影響しません。
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
     * `null` を設定すると表示名がクリアされます。
     * 文字列からの変換ではなく、Adventure の [Component] を直接扱いたい場合に使用します。
     */
    var customName: Component?
        get() = withMeta { it.customName() }
        set(value) {
            itemStack.editMeta { it.customName(value) }
        }

    /**
     * アイテムの表示名（Legacy 形式 - 色コード `§x` 使用）。
     *
     * 既存の Legacy 系コードをそのまま使いたい場合に便利です。
     * 内部的には [customName] と相互変換されるため、
     * 取得時には Component から Legacy へ、設定時には Legacy から Component へ変換されます。
     *
     * @see customName
     * @see customNameMiniMessage
     */
    var customNameText: String?
        get() = customName?.toLegacy()
        set(value) {
            customName = value?.byLegacy()
        }

    /**
     * アイテムの表示名（MiniMessage 形式 - タグベース）。
     *
     * `"<yellow>My Item"` や `"<red><bold>Rare"` のような書式をそのまま設定できます。
     * グラデーションや装飾付きのテキストを簡潔に書きたいときに便利です。
     * 内部的には [customName] と相互変換されます。
     *
     * @see customName
     * @see customNameText
     */
    var customNameMiniMessage: String?
        get() = customName?.toMiniMessage()
        set(value) {
            customName = value?.byMiniMessage()
        }

    /**
     * アイテムの説明文（Lore）- Component リスト形式。
     *
     * 各 [Component] が Lore の 1 行として扱われます。
     * `null` を設定すると Lore をクリアします。
     * 複雑な装飾やクリックイベントなど、Component ならではの表現を使いたい場合に便利です。
     */
    var lore: List<Component>?
        get() = withMeta { it.lore() }
        set(value) {
            itemStack.editMeta { it.lore(value) }
        }

    /**
     * アイテムの説明文（Lore）- Legacy 形式（色コード `§x` 使用）。
     *
     * 既存の色コード資産をそのまま利用したい場合に使えます。
     * 内部的には [lore] と相互変換され、各行ごとに Legacy 文字列と Component を変換します。
     *
     * @see lore
     * @see loreMiniMessage
     */
    var loreText: List<String>?
        get() = lore?.map { it.toLegacy() }
        set(value) {
            lore = value?.map { it.byLegacy() }
        }

    /**
     * アイテムの説明文（Lore）- MiniMessage 形式（タグベース）。
     *
     * `listOf("<gray>説明1", "<gray>説明2")` のように複数行を簡潔に記述できます。
     * 内部的には [lore] と相互変換されるため、必要に応じて Component ベースにも展開されます。
     *
     * @see lore
     * @see loreText
     */
    var loreMiniMessage: List<String>?
        get() = lore?.map { it.toMiniMessage() }
        set(value) {
            lore = value?.map { it.byMiniMessage() }
        }

    /**
     * アイテムの custom model data。
     *
     * Paper の custom model data component を 1 つの `Float` として扱います。
     * 値が複数入っている場合は先頭の値を返します。
     * `null` を設定すると保存済みの値を空にします。
     */
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

    /**
     * アイテムの item model キー。
     *
     * Paper の `DataComponentTypes.ITEM_MODEL` を使って、
     * `namespace:path` 形式のモデル識別子を設定できます。
     * `null` を設定すると item model を削除します。
     */
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
     * 設定時は既存の hidden components を維持しつつ、`hideTooltip` のみを書き換えます。
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


    /**
     * 指定した Data Component に値を直接設定します。
     *
     * 既存値の参照や変換が不要な場合に使います。
     *
     * @param componentType 設定対象の Data Component
     * @param value 設定する値
     */
    fun <T : Any> setData(componentType: DataComponentType.Valued<T>, value: T) {
        itemStack.setData(componentType, value)
    }

    /**
     * 指定した Data Component を、既存値を参照しながら構築して設定します。
     *
     * `valueBuilder` には現在の値（存在しない場合は `null`）が渡されるため、
     * 既存設定を引き継いだうえで一部だけ変更したい場合に便利です。
     *
     * @param componentType 設定対象の Data Component
     * @param valueBuilder 既存値を基に新しい値を組み立てるビルダー
     */
    fun <T: Any> setData(componentType: DataComponentType.Valued<T>, valueBuilder: (default: T?) -> DataComponentBuilder<T>) {
        val defaultValue = itemStack.getData(componentType)
        val newValue = valueBuilder(defaultValue).build()
        itemStack.setData(componentType, newValue)
    }

    /**
     * PersistentDataContainer に値を保存します。
     *
     * `NamespacedKey` を明示的に指定したい場合に使用します。
     *
     * @param key 保存先のキー
     * @param type 保存するデータ型
     * @param value 保存する値
     */
    fun <P: Any, C: Any> setPersistentData(key: NamespacedKey, type: PersistentDataType<P, C>, value: C) {
        itemStack.editMeta { meta ->
            meta.persistentDataContainer.set(key, type, value)
        }
    }

    /**
     * PersistentDataContainer に値を保存します。
     *
     * 文字列キーを渡すと、[MJavaPlugin.plugin] の名前空間で [NamespacedKey] を自動生成します。
     *
     * @param key 保存先のキー文字列
     * @param type 保存するデータ型
     * @param value 保存する値
     */
    fun <P: Any, C: Any> setPersistentData(key: String, type: PersistentDataType<P, C>, value: C) {
        val namespacedKey = NamespacedKey(MJavaPlugin.plugin, key)
        setPersistentData(namespacedKey, type, value)
    }

    /**
     * Lore（説明文）を Component ビルダーで設定します。
     *
     * [UnaryPlusBuilder] を使うことで、`+` 演算子で複数行を直感的に追加できます。
     * Component ベースで各行を組み立てたいときに利用します。
     *
     * ### 使用例
     *
     * ```kotlin
     * lore {
     *     + Component.text("Line 1")
     *     + Component.text("Line 2")
     * }
     * ```
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
     * 既存の色コード文字列をそのまま並べたい場合に便利です。
     * [UnaryPlusBuilder] を使うことで、1 行ずつ追加しながら簡潔に記述できます。
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
     * 色付きテキストや装飾を短く記述したい場合に適しています。
     * [UnaryPlusBuilder] を用いて複数行を順番に追加できます。
     *
     * ### 使用例
     *
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
     * 返された ItemStack は内部のスタックを複製したものです。
     * 呼び出し側で変更しても、このビルダーが保持している状態には影響しません。
     *
     * @return 生成した ItemStack（複製）
     */
    override fun build(): ItemStack {
        return itemStack.clone()
    }
}