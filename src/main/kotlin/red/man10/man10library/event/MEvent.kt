package red.man10.man10library.event

import org.bukkit.event.Event
import org.bukkit.event.EventPriority

/**
 * Bukkit のイベントリスナー登録をまとめて扱うためのコンテナ。
 *
 * `MEventUnit` を内部に保持し、`register` で追加したイベントハンドラを一括で管理します。
 * 個別のイベント購読を自前で保持する必要がなく、`unregisterAll` を呼ぶだけでまとめて解除できます。
 *
 * ### 使い方の例
 *
 * ```kotlin
 * val events = MEvent()
 *
 * events.register<PlayerJoinEvent> { event ->
 *     event.player.sendMessage("Welcome!")
 * }
 * ```
 *
 * 必要なくなったら、保持しているすべての登録を解除します。
 *
 * ```kotlin
 * events.unregisterAll()
 * ```
 *
 * @see MEventUnit
 */
@Suppress("unused")
class MEvent {

    private val mEventUnits = mutableListOf<MEventUnit<*>>()

    /**
     * 指定した Bukkit イベントを登録します。
     *
     * @param clazz 監視したいイベントクラス
     * @param priority イベントの実行優先度。省略時は [EventPriority.NORMAL]
     * @param handler イベント発生時に実行する処理
     * @return 登録した [MEventUnit]
     */
    fun <T: Event> register(clazz: Class<T>, priority: EventPriority = EventPriority.NORMAL, handler: (T) -> Unit): MEventUnit<T> {
        val unit = MEventUnit(clazz, priority, handler)
        mEventUnits.add(unit)
        return unit
    }

    /**
     * 型推論を利用して Bukkit イベントを登録します。
     *
     * クラスを明示せず、`register<FooEvent> { ... }` の形式で簡潔に書きたい場合に使います。
     *
     * @param priority イベントの実行優先度。省略時は [EventPriority.NORMAL]
     * @param handler イベント発生時に実行する処理
     * @return 登録した [MEventUnit]
     */
    inline fun <reified T: Event> register(priority: EventPriority = EventPriority.NORMAL, noinline handler: (T) -> Unit): MEventUnit<T> {
        return register(T::class.java, priority, handler)
    }

    /**
     * このインスタンスが保持しているすべてのイベント登録を解除します。
     *
     * 機能の有効/無効を切り替えるタイミングで呼び出す想定です。
     */
    fun unregisterAll() {
        mEventUnits.forEach { it.unregister() }
        mEventUnits.clear()
    }
}