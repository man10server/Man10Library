package red.man10.man10library.inventory.itemStack

import org.bukkit.inventory.ItemStack

/**
 * アイテムスタックのビルダーインターフェイス。
 *
 * [MItemStack] および [MSimpleItemStack] の共通機能を定義しています。
 *
 * @see MItemStack アイテムの詳細設定が可能な実装クラス
 * @see MSimpleItemStack シンプルなアイテム操作用実装クラス
 */
interface IMItemStack {
    /**
     * このアイテムを Bukkit の ItemStack に変換して返します。
     *
     * 通常、返されたアイテムスタックは設定した内容の複製です。
     *
     * @return 生成した ItemStack
     */
    fun build(): ItemStack
}