package red.man10.man10library.inventory.itemStack.input

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import red.man10.man10library.inventory.MInventory
import red.man10.man10library.inventory.itemStack.MInventoryItem
import red.man10.man10library.utils.UnaryPlusBuilder
import red.man10.man10library.utils.byLegacy
import red.man10.man10library.utils.byMiniMessage
import red.man10.man10library.utils.toLegacy
import red.man10.man10library.utils.toMiniMessage

/**
 * プレイヤー入力を受け取るためのインベントリアイテム。
 *
 * このクラスは、クリック時にチャット入力などから値を受け取り、入力内容を [type] に従って
 * 変換したうえでコールバックへ渡します。
 * 空文字が入力された場合は `null` として扱えるため、任意入力を許可したいケースに適しています。
 *
 * 入力を受け付けると、対象プレイヤーには [message] が送信され、入力セッションが [MInputItemManager]
 * に登録されます。入力成功時は [onNullableEnter]、キャンセル時は [onCancelled] が呼び出されます。
 *
 * ### 主な用途
 *
 * - 数値や文字列などの値をプレイヤーから直接入力させる
 * - 空入力を許可し、未設定状態を `null` として扱う
 * - 入力後に自動で元のインベントリへ戻す
 *
 * ### 使用例
 *
 * ```kotlin
 * setNullableInput(0, Material.STONE, String::class.java) {
 *     message = Component.text("チャットに値を入力してください")
 *     onNullableEnter {
 *         player.sendMessage("入力値: ${value ?: "null"}")
 *     }
 *     onCancelled {
 *         sendMessage("入力をキャンセルしました")
 *     }
 * }
 * ```
 *
 * @param inventory この入力アイテムを配置している [MInventory]
 * @param mInventoryItem 元になる [MInventoryItem]
 * @param type 入力文字列を変換する対象の型
 *
 * @see MInputItem
 * @see MInputItemManager
 */
@Suppress("unused")
open class MNullableInputItem<T: Any>(
    protected val inventory: MInventory,
    mInventoryItem: MInventoryItem,
    val type: Class<T>,
): MInventoryItem(mInventoryItem) {

    /**
     * クリック時にプレイヤーへ送信する案内メッセージ。
     *
     * このメッセージを送信したあと、入力セッションを開始します。
     */
    var message: Component = Component.empty()

    /**
     * [message] をレガシー形式の文字列として取得・設定します。
     *
     * @see message
     */
    var messageText: String
        get() = message.toLegacy()
        set(value) {
            message = value.byLegacy()
        }

    /**
     * [message] をMiniMessage形式の文字列として取得・設定します。
     *
     * @see message
     */
    var messageMiniMessage: String
        get() = message.toMiniMessage()
        set(value) {
            message = value.byMiniMessage()
        }

    /**
     * 入力値の変換に失敗したときに送信するメッセージを生成します。
     */
    var errorMessage: (erroredInput: String) -> Component = { Component.empty() }

    /**
     * [errorMessage] をレガシー形式の文字列として取得・設定します。
     *
     * @see errorMessage
     */
    var errorMessageText: (erroredInput: String) -> String
        get() = { errorMessage(it).toLegacy() }
        set(value) {
            errorMessage = { value(it).byLegacy() }
        }

    /**
     * [errorMessage] をMiniMessage形式の文字列として取得・設定します。
     *
     * @see errorMessage
     */
    var errorMessageMiniMessage: (erroredInput: String) -> String
        get() = { errorMessage(it).toMiniMessage() }
        set(value) {
            errorMessage = { value(it).byMiniMessage() }
        }

    /**
     * 入力開始を許可するクリック種別の集合。
     *
     * 空の場合はクリック種別による制限を行いません。
     */
    var allowedClickTypes: Set<ClickType> = setOf()

    /**
     * 入力成功後に元のインベントリを自動で開き直すかどうか。
     */
    var openInventoryAfterInput: Boolean = true

    /**
     * 入力キャンセル後に元のインベントリを自動で開き直すかどうか。
     */
    var openInventoryAfterCancel: Boolean = true

    /**
     * 空入力も許可する入力完了コールバック。
     */
    protected var onNullableEnter: (MNullableInputContext<T>.() -> Unit)? = null

    /**
     * 入力キャンセル時に実行されるコールバック。
     */
    protected var onCancelled: (Player.() -> Unit)? = null

    init {
        @Suppress("UNUSED_EXPRESSION")
        MInputItemManager // クラス初期化のためのダミー参照

        onClick {
            if (allowedClickTypes.isNotEmpty() && inventoryClickEvent.click !in allowedClickTypes) {
                return@onClick
            }

            player.sendMessage(message)

            MInputItemManager.inputSessions[player.uniqueId] = createSession(player, message.toString())
            inventory.closeSilently(player)
        }
    }


    /**
     * この入力アイテムを反応させるクリック種別を設定します。
     *
     * 指定したクリック種別以外では入力処理を開始しません。未設定のままの場合は、
     * すべてのクリック種別を対象にします。
     *
     * @param builder 許可する [ClickType] を登録するビルダー
     */
    fun allowClickTypes(builder: UnaryPlusBuilder<ClickType>.() -> Unit) {
        val clickTypeBuilder = UnaryPlusBuilder<ClickType>()
        clickTypeBuilder.builder()
        allowedClickTypes = clickTypeBuilder.build().toSet()
    }

    /**
     * 入力成功時のコールバックを登録します。
     *
     * 空入力の場合は `null` を含む [MNullableInputContext] が渡されます。
     *
     * @param listener 入力完了時に実行する処理
     */
    open fun onNullableEnter(listener: MNullableInputContext<T>.() -> Unit) {
        onNullableEnter = listener
    }

    /**
     * 入力キャンセル時のコールバックを登録します。
     *
     * @param listener キャンセル時に実行する処理
     */
    open fun onCancelled(listener: Player.() -> Unit) {
        onCancelled = listener
    }

    /**
     * プレイヤーごとの入力セッションを生成します。
     *
     * 入力値の検証や変換、成功時・失敗時の挙動をまとめて管理する [InputSession] を返します。
     *
     * @param player 入力対象のプレイヤー
     * @param input 現在の入力文字列
     * @return 入力処理に使用するセッション
     */
    open fun createSession(player: Player, input: String): InputSession {
        return InputSession(
            onEnter = { msg, player ->
                val isBlank = msg.isBlank()
                if (isBlank) {
                    onNullableEnter?.invoke(MNullableInputContext(null, player))
                    if (openInventoryAfterInput) {
                        inventory.open(player)
                    }
                    return@InputSession true
                }

                val parsedValue = MInputItemManager.parseInput(msg, type)
                if (parsedValue == null) {
                    player.sendMessage(errorMessage(msg))
                    if (openInventoryAfterInput) {
                        inventory.open(player)
                    }
                    return@InputSession false
                }

                onNullableEnter?.invoke(MNullableInputContext(parsedValue, player))
                if (openInventoryAfterInput) {
                    inventory.open(player)
                }
                return@InputSession true
            },
            onCancelled = { player ->
                onCancelled?.invoke(player)
                if (openInventoryAfterCancel) {
                    inventory.open(player)
                }
            }
        )
    }

    /**
     * [onNullableEnter] に渡される入力結果コンテキスト。
     *
     * @param T 変換後の入力型
     * @property value 変換された入力値。空入力時は `null`
     * @property player 入力したプレイヤー
     */
    data class MNullableInputContext<T: Any>(
        val value: T?,
        val player: Player
    )
}