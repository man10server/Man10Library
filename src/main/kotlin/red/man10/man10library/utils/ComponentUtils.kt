package red.man10.man10library.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/** Legacy 形式（`§` コード）の変換処理を担当するシリアライザー。 */
private val legacyComponentSerializer = LegacyComponentSerializer.builder()
    .hexColors()
    .useUnusualXRepeatedCharacterHexFormat()
    .build()

/** MiniMessage 形式（`<color>` タグなど）の変換処理を担当するシリアライザー。 */
private val miniMessageSerializer = MiniMessage.miniMessage()


/**
 * Legacy 形式の文字列を Component に変換します。
 *
 * @receiver 変換対象の Legacy 形式の文字列
 * @return 変換された Component オブジェクト
 */
fun String.byLegacy(): Component {
    return legacyComponentSerializer.deserialize(this)
}

/**
 * MiniMessage 形式の文字列を Component に変換します。
 *
 * @receiver 変換対象の MiniMessage 形式の文字列
 * @return 変換された Component オブジェクト
 */
fun String.byMiniMessage(): Component {
    return miniMessageSerializer.deserialize(this)
}

/**
 * Component を Legacy 形式の文字列に変換します。
 *
 * @receiver 変換対象の Component オブジェクト
 * @return 変換された Legacy 形式の文字列
 */
fun Component.toLegacy(): String {
    return legacyComponentSerializer.serialize(this)
}

/**
 * Component を MiniMessage 形式の文字列に変換します。
 *
 * @receiver 変換対象の Component オブジェクト
 * @return 変換された MiniMessage 形式の文字列
 */
fun Component.toMiniMessage(): String {
    return miniMessageSerializer.serialize(this)
}