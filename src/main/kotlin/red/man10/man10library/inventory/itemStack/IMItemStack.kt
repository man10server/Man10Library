package red.man10.man10library.inventory.itemStack

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.inventory.ItemStack

/**
 * アイテムスタックのビルダーインターフェイス。
 *
 * [MItemStack] および [MSimpleItemStack] の共通機能を定義しています。
 * テキスト形式の変換（Legacy、MiniMessage）とアイテムのビルドをサポートします。
 *
 * ### テキスト形式の変換
 *
 * このインターフェイスは以下の3つのテキスト形式をサポートしています：
 *
 * - **Component**: Minecraft の最新テキスト形式
 * - **Legacy**: 色コード（`§x` など）を使った従来形式
 * - **MiniMessage**: より簡潔な書き方（`<red><bold>text</bold></red>` など）
 *
 * @see MItemStack アイテムの詳細設定が可能な実装クラス
 * @see MSimpleItemStack シンプルなアイテム操作用実装クラス
 */
interface IMItemStack {

    companion object {
        /** Legacy 形式（`§` コード）の変換処理を担当するシリアライザー。 */
        internal val legacyComponentSerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()

        /** MiniMessage 形式（`<color>` タグなど）の変換処理を担当するシリアライザー。 */
        internal val miniMessageSerializer = MiniMessage.miniMessage()
    }

    /**
     * このアイテムを Bukkit の ItemStack に変換して返します。
     *
     * 通常、返されたアイテムスタックは設定した内容の複製です。
     *
     * @return 生成した ItemStack
     */
    fun build(): ItemStack

    /**
     * [Component] を Legacy 形式（色コード）の文字列に変換します。
     *
     * @param component 変換対象の Component
     * @return Legacy 形式の文字列
     *
     * @see fromLegacy
     */
    fun toLegacy(component: Component): String {
        return legacyComponentSerializer.serialize(component)
    }

    /**
     * Legacy 形式（色コード）の文字列を [Component] に変換します。
     *
     * @param text Legacy 形式の文字列
     * @return 変換された Component
     *
     * @see toLegacy
     */
    fun fromLegacy(text: String): Component {
        return legacyComponentSerializer.deserialize(text)
    }

    /**
     * [Component] を MiniMessage 形式（タグベース）の文字列に変換します。
     *
     * @param component 変換対象の Component
     * @return MiniMessage 形式の文字列（例：`<red>text</red>`）
     *
     * @see fromMiniMessage
     */
    fun toMiniMessage(component: Component): String {
        return miniMessageSerializer.serialize(component)
    }

    /**
     * MiniMessage 形式（タグベース）の文字列を [Component] に変換します。
     *
     * @param text MiniMessage 形式の文字列（例：`<red>text</red>`）
     * @return 変換された Component
     *
     * @see toMiniMessage
     */
    fun fromMiniMessage(text: String): Component {
        return miniMessageSerializer.deserialize(text)
    }
}