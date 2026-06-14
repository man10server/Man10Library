package red.man10.man10library.inventory

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Range
import red.man10.man10library.event.MEvent
import red.man10.man10library.inventory.context.InventoryClickContext
import red.man10.man10library.inventory.context.InventoryCloseContext
import red.man10.man10library.inventory.itemStack.MInventoryItem
import red.man10.man10library.inventory.itemStack.input.MInputItem
import red.man10.man10library.inventory.itemStack.input.MNullableInputItem
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Minecraft のインベントリを簡潔に操作するための抽象基底クラス。
 *
 * このクラスを継承することで、アイテムクリックやインベントリクローズなどのイベントハンドリング、
 * アイテムの配置やレンダリングを自動的に管理できます。
 *
 * 本クラスは [InventoryHolder] インターフェイスを実装しており、
 * Bukkit の [Inventory] API と統合されています。
 *
 * ### 基本的な使用例
 *
 * ```kotlin
 * class MyInventory : MInventory("My Inventory", 3) {
 *     override fun renderContents() {
 *         // インベントリのコンテンツを設定
 *         set(0, Material.DIAMOND) {
 *             customNameMiniMessage = "<yellow>Diamond"
 *             onClick {
 *                 player.sendMessage("Diamond clicked!")
 *             }
 *         }
 *     }
 * }
 *
 * // プレイヤーに表示
 * val inventory = MyInventory()
 * inventory.open(player)
 * ```
 *
 * ### アイテムの設定方法
 *
 * インベントリにはアイテムを複数の方法で設定できます：
 *
 * - `set(Int, ...)` - 単一のスロットに設定
 * - `set(IntArray, ...)` - 複数のスロットに設定
 * - `set(IntRange, ...)` - スロット範囲に設定
 *
 * アイテムは [MInventoryItem], [ItemStack], または [Material] として渡せます。
 *
 * ### イベントハンドリング
 *
 * `onClick` で各アイテムのクリック処理を定義します：
 *
 * ```kotlin
 * set(0, Material.DIAMOND) {
 *     onClick {
 *         // this は InventoryClickContext
 *         player.sendMessage("Clicked!")
 *     }
 * }
 * ```
 *
 * インベントリクローズ時の処理は `onClose` リストに登録します：
 *
 * ```kotlin
 * onClose {
 *     // this は InventoryCloseContext
 *     player.sendMessage("Inventory closed!")
 * }
 * ```
 *
 * @param title インベントリの表示名（[Component] 形式）
 * @param row インベントリの行数（1～6 の値）
 *
 * @see InventoryClickContext インベントリクリック時のコンテキスト
 * @see InventoryCloseContext インベントリクローズ時のコンテキスト
 * @see MInventoryItem インベントリアイテム
 * @see red.man10.man10library.inventory.builtin.LargeMInventory ページング対応のインベントリ
 */
@Suppress("unused")
abstract class MInventory(
    title: Component,
    row: @Range(from = 1, to = 6) Int
): InventoryHolder {

    /**
     * String で タイトルを指定するコンストラクタの代替。
     *
     * 内部的には [Component.text] で [Component] に変換されます。
     *
     * @param title インベントリの表示名（String）
     * @param row インベントリの行数（1～6 の値）
     */
    constructor(
        title: String,
        row: @Range(from = 1, to = 6) Int
    ): this(Component.text(title), row)

    /** インベントリ内のアイテムをスロット番号でマッピング。スレッドセーフです。 */
    val items = ConcurrentHashMap<Int, MInventoryItem>()

    /** インベントリクローズ時に実行されるコールバックのリスト。 */
    private val onClose: MutableList<InventoryCloseContext.() -> Unit> = mutableListOf()

    private val closeSilentlyPlayers = mutableSetOf<UUID>()

    /** Bukkit の Inventory オブジェクト。 */
    private val inventory = Bukkit.createInventory(this, row * 9, title)

    /**
     * `true` の場合、[set] メソッドでアイテムを設定した時点でインベントリに反映されます。
     * `false` の場合は [render] メソッド実行時に反映されます。
     *
     * デフォルトは `false` です。
     */
    open val renderOnSet = false

    companion object {
        /** インベントリのイベント処理を一元管理するイベントハンドラー。 */
        val mEvent = MEvent()

        init {
            // InventoryClickEvent のハンドリング
            mEvent.register<InventoryClickEvent> { e ->
                if (e.isCancelled) return@register
                val holder = e.inventory.holder as? MInventory ?: return@register
                val item = holder.items[e.rawSlot] ?: return@register
                e.isCancelled = true
                val context = InventoryClickContext(e, item)
                item.handleClick(context)
            }

            // InventoryCloseEvent のハンドリング
            mEvent.register<InventoryCloseEvent> { e ->
                val holder = e.inventory.holder as? MInventory ?: return@register
                if (holder.closeSilentlyPlayers.remove(e.player.uniqueId)) {
                    // クローズを無効化している場合は処理しない
                    return@register
                }
                val context = InventoryCloseContext(e)
                holder.handleClose(context)
            }
        }
    }

    /**
     * Bukkit の Inventory オブジェクトを取得します。
     *
     * @return このホルダーが管理する Inventory
     */
    override fun getInventory(): Inventory {
        return inventory
    }

    /**
     * インベントリのコンテンツをレンダリングします。
     *
     * このメソッドはサブクラスで実装し、[set] メソッドを使用してアイテムを配置します。
     * [render] メソッドから呼び出されます。
     *
     * @see render
     */
    abstract fun renderContents()

    /**
     * インベントリのコンテンツを再レンダリングします。
     *
     * `renderOnSet` が `false` の場合：
     * 1. 既存のアイテムと内部マップをクリア
     * 2. [renderContents] を実行
     * 3. アイテムを Bukkit インベントリに反映
     *
     * `renderOnSet` が `true` の場合：
     * - [renderContents] のみ実行（アイテム反映は [set] 時に行われる）
     *
     * ページング対応などで複数回レンダリングする場合に便利です。
     *
     * @see renderContents
     */
    fun render() {
        if (!renderOnSet) {
            items.clear()
            inventory.clear()
        }

        renderContents()

        if (!renderOnSet) {
            items.forEach { (slot, item) ->
                inventory.setItem(slot, item.itemStack)
            }
        }
    }

    /**
     * インベントリをプレイヤーに開きます。
     *
     * 実行前に [render] メソッドを呼び出し、コンテンツを更新します。
     *
     * @param player インベントリを開くプレイヤー
     */
    fun open(player: Player) {
        render()
        player.openInventory(inventory)
    }

     /**
      * インベントリをプレイヤーに閉じさせますが、[onClose] コールバックを実行しません。
      *
      * プレイヤーがこのインベントリを閉じたときに、[onClose] リストのコールバックが実行されるのを
      * 防ぎたい場合に使用します。
      *
      * @param player インベントリを閉じるプレイヤー
      */
     fun closeSilently(player: Player) {
         closeSilentlyPlayers.add(player.uniqueId)
         player.closeInventory()
     }

    /**
     * インベントリクローズ時に実行するコールバックを追加します。
     *
     * 追加されたコールバックは、プレイヤーがこのインベントリを閉じたときに順番に実行されます。
     * `this` には [InventoryCloseContext] が渡されるため、[InventoryCloseContext.player] などを利用できます。
     *
     * ### 使用例
     *
     * ```kotlin
     * onClose {
     *     player.sendMessage("Closed!")
     * }
     * ```
     *
     * @param callback インベントリクローズ時に実行するラムダ
     */
    fun onClose(callback: InventoryCloseContext.() -> Unit) {
        onClose.add(callback)
    }

    /**
     * 複数のスロットに [MInventoryItem] を同時に設定します。
     *
     * @param slots 設定対象のスロット番号の配列
     * @param mInventoryItem 設定するアイテム
     *
     * @see set
     */
    fun set(slots: IntArray, mInventoryItem: MInventoryItem, init: MInventoryItem.() -> Unit = {}) {
        mInventoryItem.apply(init)
        val itemStack = mInventoryItem.build()
        slots.forEach { slot ->
            items[slot] = mInventoryItem
        }
        if (renderOnSet) {
            slots.forEach { slot ->
                inventory.setItem(slot, itemStack)
            }
        }
    }

    /**
     * 複数のスロットに ItemStack を設定し、[MInventoryItem] デコレータで設定します。
     *
     * @param slots 設定対象のスロット番号の配列
     * @param itemStack 設定する ItemStack
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     *
     * @see set
     */
    fun set(slots: IntArray, itemStack: ItemStack, init: MInventoryItem.() -> Unit = {}) {
        val mInventoryItem = MInventoryItem(itemStack)
        set(slots, mInventoryItem, init)
    }

    /**
     * 複数のスロットに Material から ItemStack を生成して設定します。
     *
     * @param slots 設定対象のスロット番号の配列
     * @param material 設定する Material
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     *
     * @see set
     */
    fun set(slots: IntArray, material: Material, init: MInventoryItem.() -> Unit = {}) {
        val mInventoryItem = MInventoryItem(material)
        set(slots, mInventoryItem, init)
    }

    /**
     * スロット範囲に [MInventoryItem] を設定します。
     *
     * @param slots 設定対象のスロット範囲（[IntRange]）
     * @param mInventoryItem 設定するアイテム
     *
     * @see set
     */
    fun set(slots: IntRange, mInventoryItem: MInventoryItem, init: MInventoryItem.() -> Unit = {}) {
        set(slots.toList().toIntArray(), mInventoryItem, init)
    }

    /**
     * スロット範囲に ItemStack を設定し、[MInventoryItem] デコレータで設定します。
     *
     * @param slots 設定対象のスロット範囲（[IntRange]）
     * @param itemStack 設定する ItemStack
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     *
     * @see set
     */
    fun set(slots: IntRange, itemStack: ItemStack, init: MInventoryItem.() -> Unit = {}) {
        set(slots.toList().toIntArray(), itemStack, init)
    }

    /**
     * スロット範囲に Material から ItemStack を生成して設定します。
     *
     * @param slots 設定対象のスロット範囲（[IntRange]）
     * @param material 設定する Material
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     *
     * @see set
     */
    fun set(slots: IntRange, material: Material, init: MInventoryItem.() -> Unit = {}) {
        set(slots.toList().toIntArray(), material, init)
    }

    /**
     * 単一のスロットに [MInventoryItem] を設定します。
     *
     * @param slot 設定対象のスロット番号
     * @param mInventoryItem 設定するアイテム
     *
     * @see set
     */
    fun set(slot: Int, mInventoryItem: MInventoryItem, init: MInventoryItem.() -> Unit = {}) {
        set(intArrayOf(slot), mInventoryItem, init)
    }

    /**
     * 単一のスロットに ItemStack を設定し、[MInventoryItem] デコレータで設定します。
     *
     * @param slot 設定対象のスロット番号
     * @param itemStack 設定する ItemStack
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     *
     * @see set
     */
    fun set(slot: Int, itemStack: ItemStack, init: MInventoryItem.() -> Unit = {}) {
        set(intArrayOf(slot), itemStack, init)
    }

    /**
     * 単一のスロットに Material から ItemStack を生成して設定します。
     *
     * @param slot 設定対象のスロット番号
     * @param material 設定する Material
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     *
     * @see set
     */
     fun set(slot: Int, material: Material, init: MInventoryItem.() -> Unit = {}) {
         set(intArrayOf(slot), material, init)
     }

    /**
     * インベントリ全体を [MInventoryItem] で埋め尽くします。
     *
     * @param mInventoryItem 設定するアイテム
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     */
    fun fill(mInventoryItem: MInventoryItem, init: MInventoryItem.() -> Unit = {}) {
        val allSlots = (0 until inventory.size).toList().toIntArray()
        set(allSlots, mInventoryItem, init)
    }

    /**
     * インベントリ全体を ItemStack で埋め尽くし、[MInventoryItem] デコレータで設定します。
     *
     * @param itemStack 設定する ItemStack
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     */
    fun fill(itemStack: ItemStack, init: MInventoryItem.() -> Unit = {}) {
        val mInventoryItem = MInventoryItem(itemStack)
        fill(mInventoryItem, init)
    }


    /**
     * インベントリ全体を Material から生成された ItemStack で埋め尽くし、[MInventoryItem] デコレータで設定します。
     *
     * @param material 設定する Material
     * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
     */
    fun fill(material: Material, init: MInventoryItem.() -> Unit = {}) {
        val mInventoryItem = MInventoryItem(material)
        fill(mInventoryItem, init)
    }

     /**
      * 複数のスロットに [MNullableInputItem] を設定します。
      *
      * これはプレイヤーの入力を受け取るために使用されるアイテムです。
      * 値が `null` である場合もハンドリングできます。
      *
      * @param slots 設定対象のスロット番号の配列
      * @param mInventoryItem 設定するアイテム
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see MNullableInputItem
      */
     fun <T: Any> setNullableInput(slots: IntArray, mInventoryItem: MInventoryItem, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         val inputItem = MNullableInputItem(this, mInventoryItem, type)
         init(inputItem)
         set(slots, inputItem)
     }

     /**
      * スロット範囲に [MNullableInputItem] を設定します。
      *
      * @param slots 設定対象のスロット範囲（[IntRange]）
      * @param mInventoryItem 設定するアイテム
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slots: IntRange, mInventoryItem: MInventoryItem, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         setNullableInput(slots.toList().toIntArray(), mInventoryItem, type, init)
     }

     /**
      * 単一のスロットに [MNullableInputItem] を設定します。
      *
      * @param slot 設定対象のスロット番号
      * @param mInventoryItem 設定するアイテム
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slot: Int, mInventoryItem: MInventoryItem, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         setNullableInput(intArrayOf(slot), mInventoryItem, type, init)
     }

     /**
      * 複数のスロットに ItemStack から作成された [MNullableInputItem] を設定します。
      *
      * @param slots 設定対象のスロット番号の配列
      * @param itemStack 設定する ItemStack
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slots: IntArray, itemStack: ItemStack, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         val mInventoryItem = MInventoryItem(itemStack)
         setNullableInput(slots, mInventoryItem, type, init)
     }

     /**
      * スロット範囲に ItemStack から作成された [MNullableInputItem] を設定します。
      *
      * @param slots 設定対象のスロット範囲（[IntRange]）
      * @param itemStack 設定する ItemStack
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slots: IntRange, itemStack: ItemStack, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         setNullableInput(slots.toList().toIntArray(), itemStack, type, init)
     }

     /**
      * 単一のスロットに ItemStack から作成された [MNullableInputItem] を設定します。
      *
      * @param slot 設定対象のスロット番号
      * @param itemStack 設定する ItemStack
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slot: Int, itemStack: ItemStack, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         setNullableInput(intArrayOf(slot), itemStack, type, init)
     }

     /**
      * 複数のスロットに Material から作成された [MNullableInputItem] を設定します。
      *
      * @param slots 設定対象のスロット番号の配列
      * @param material 設定する Material
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slots: IntArray, material: Material, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         val mInventoryItem = MInventoryItem(material)
         setNullableInput(slots, mInventoryItem, type, init)
     }

     /**
      * スロット範囲に Material から作成された [MNullableInputItem] を設定します。
      *
      * @param slots 設定対象のスロット範囲（[IntRange]）
      * @param material 設定する Material
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slots: IntRange, material: Material, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         setNullableInput(slots.toList().toIntArray(), material, type, init)
     }

     /**
      * 単一のスロットに Material から作成された [MNullableInputItem] を設定します。
      *
      * @param slot 設定対象のスロット番号
      * @param material 設定する Material
      * @param type 入力値の型（例：String::class.java）
      * @param init [MNullableInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setNullableInput
      */
     fun <T: Any> setNullableInput(slot: Int, material: Material, type: Class<T>, init: MNullableInputItem<T>.() -> Unit = {}) {
         setNullableInput(intArrayOf(slot), material, type, init)
     }

     /**
      * 複数のスロットに [MInputItem] を設定します。
      *
      * これはプレイヤーの入力を受け取るために使用されるアイテムです。
      * [setNullableInput] と異なり、入力値は `null` であってはいけません。
      *
      * @param slots 設定対象のスロット番号の配列
      * @param mInventoryItem 設定するアイテム
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see MInputItem
      * @see setNullableInput
      */
     fun <T: Any> setInput(slots: IntArray, mInventoryItem: MInventoryItem, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         val inputItem = MInputItem(this, mInventoryItem, type)
         init(inputItem)
         set(slots, inputItem)
     }

     /**
      * スロット範囲に [MInputItem] を設定します。
      *
      * @param slots 設定対象のスロット範囲（[IntRange]）
      * @param mInventoryItem 設定するアイテム
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slots: IntRange, mInventoryItem: MInventoryItem, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         setInput(slots.toList().toIntArray(), mInventoryItem, type, init)
     }

     /**
      * 単一のスロットに [MInputItem] を設定します。
      *
      * @param slot 設定対象のスロット番号
      * @param mInventoryItem 設定するアイテム
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slot: Int, mInventoryItem: MInventoryItem, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         setInput(intArrayOf(slot), mInventoryItem, type, init)
     }

     /**
      * 複数のスロットに ItemStack から作成された [MInputItem] を設定します。
      *
      * @param slots 設定対象のスロット番号の配列
      * @param itemStack 設定する ItemStack
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slots: IntArray, itemStack: ItemStack, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         val mInventoryItem = MInventoryItem(itemStack)
         setInput(slots, mInventoryItem, type, init)
     }

     /**
      * スロット範囲に ItemStack から作成された [MInputItem] を設定します。
      *
      * @param slots 設定対象のスロット範囲（[IntRange]）
      * @param itemStack 設定する ItemStack
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slots: IntRange, itemStack: ItemStack, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         setInput(slots.toList().toIntArray(), itemStack, type, init)
     }

     /**
      * 単一のスロットに ItemStack から作成された [MInputItem] を設定します。
      *
      * @param slot 設定対象のスロット番号
      * @param itemStack 設定する ItemStack
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slot: Int, itemStack: ItemStack, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         setInput(intArrayOf(slot), itemStack, type, init)
     }

     /**
      * 複数のスロットに Material から作成された [MInputItem] を設定します。
      *
      * @param slots 設定対象のスロット番号の配列
      * @param material 設定する Material
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slots: IntArray, material: Material, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         val mInventoryItem = MInventoryItem(material)
         setInput(slots, mInventoryItem, type, init)
     }

     /**
      * スロット範囲に Material から作成された [MInputItem] を設定します。
      *
      * @param slots 設定対象のスロット範囲（[IntRange]）
      * @param material 設定する Material
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slots: IntRange, material: Material, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         setInput(slots.toList().toIntArray(), material, type, init)
     }

     /**
      * 単一のスロットに Material から作成された [MInputItem] を設定します。
      *
      * @param slot 設定対象のスロット番号
      * @param material 設定する Material
      * @param type 入力値の型（例：String::class.java）
      * @param init [MInputItem] の初期化ラムダ（デフォルト：空）
      *
      * @see setInput
      */
     fun <T: Any> setInput(slot: Int, material: Material, type: Class<T>, init: MInputItem<T>.() -> Unit = {}) {
         setInput(intArrayOf(slot), material, type, init)
     }

    /**
     * インベントリクローズイベントのハンドリング。
     *
     * `onClose` リストに登録されたコールバックを実行します。
     *
     * @param context インベントリクローズのコンテキスト
     */
    internal fun handleClose(context: InventoryCloseContext) {
        onClose.forEach { it(context) }
    }
}