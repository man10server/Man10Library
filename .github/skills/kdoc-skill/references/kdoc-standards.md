# KDoc ベストプラクティス（Man10Library 準拠）

このドキュメントは、Man10Library プロジェクトの高品質な KDoc を基準として、新規 KDoc を作成・修正する際の標準を定めます。

## 目次

1. [クラス KDoc](#クラス-kdoc)
2. [メソッド・関数 KDoc](#メソッド関数-kdoc)
3. [プロパティ KDoc](#プロパティ-kdoc)
4. [エッジケースと例外](#エッジケースと例外)
5. [言語・スタイル](#言語スタイル)

---

## クラス KDoc

### 基本構造

```kotlin
/**
 * [1行での目的]
 *
 * [詳細説明：このクラスが何をするか、どんな問題を解決するか]
 *
 * [本クラスがどのように機能するかの説明（必要に応じて）]
 *
 * ### [サブセクション1]
 *
 * [セクション固有の説明]
 *
 * ```kotlin
 * [コード例]
 * ```
 *
 * ### [サブセクション2]
 *
 * [別のセクションについての説明]
 *
 * @param [パラメータ名] [説明]
 * @see [関連クラス・インターフェイス]
 */
class MyClass(param: Type) {
    // ...
}
```

### 1行目の目的

- **簡潔に** - 目的を1文で説明します
- **動詞で開始** - 「〜をするクラス」「〜を管理するクラス」という形式
- **濁点を避ける** - 接続詞なしで完結させます

**よい例：**
```kotlin
/**
 * Minecraft のインベントリを簡潔に操作するための抽象基底クラス。
 */
class MInventory { }
```

**悪い例：**
```kotlin
/**
 * このクラスはインベントリに関連する機能を提供するクラスです。
 */
class MInventory { }
```

### 詳細説明

- クラスの責務を説明
- 使用される主要な概念（デザインパターン）を簡潔に述べる
- **継承や実装の場合** - その理由や行う主要な処理をコンテキストで説明

**例：**
```kotlin
/**
 * コマンド定義をまとめる抽象ベースクラス。
 *
 * サブクラスはフィールドに `@MCommandBody` アノテーションを付与して `MCommandObject` を定義します。
 * コンストラクタで受け取った {@link Commands} レジストラを使用して、内部で定義されたコマンドを自動的に登録します。
 */
abstract class MCommand(registrar: Commands) { }
```

### 使用例

**要件：**
- 実装例を含める（コンストラクタ引数の設定方法、メイン処理の呼び出し方）
- 可能な限り最小限の例（最短 5～10 行）

**コード例の形式：**
```
```kotlin
[実装コード]
```
```

**例：**
```kotlin
/**
 * ...
 *
 * ### 基本的な使用例
 *
 * ```kotlin
 * class MyInventory : MInventory("My Inventory", 3) {
 *     override fun renderContents() {
 *         set(0, Material.DIAMOND) {
 *             customNameMiniMessage = "<yellow>Diamond"
 *             onClick {
 *                 player.sendMessage("Diamond clicked!")
 *             }
 *         }
 *     }
 * }
 *
 * val inventory = MyInventory()
 * inventory.open(player)
 * ```
 */
```

### サブセクション（必要に応じて）

複数の概念・機能が含まれる場合、セクションごとに分割：

```kotlin
/**
 * ...
 *
 * ### アイテムの設定方法
 *
 * インベントリにはアイテムを複数の方法で設定できます：
 *
 * - `set(Int, ...)` - 単一のスロットに設定
 * - `set(IntArray, ...)` - 複数のスロットに設定
 * - `set(IntRange, ...)` - スロット範囲に設定
 *
 * ### イベントハンドリング
 *
 * `onClick` で各アイテムのクリック処理を定義します：
 *
 * ```kotlin
 * set(0, Material.DIAMOND) {
 *     onClick {
 *         player.sendMessage("Clicked!")
 *     }
 * }
 * ```
 */
```

### パラメータ（@param）

```kotlin
/**
 * ...
 *
 * @param title インベントリの表示名（[Component] 形式）
 * @param row インベントリの行数（1～6 の値）
 */
class MInventory(
    title: Component,
    row: @Range(from = 1, to = 6) Int
)
```

**ポイント：**
- **型の説明** - 特に Optional や Range の場合、有効値を記述
- **制約** - 「1～6」などの制約は明記
- **代替手段** - オーバーロードコンストラクタがある場合は言及

**例：**
```kotlin
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
```

### リンク（@see）

```kotlin
/**
 * ...
 *
 * @see InventoryClickContext インベントリクリック時のコンテキスト
 * @see MInventoryItem インベントリアイテム
 * @see red.man10.man10library.inventory.builtin.LargeMInventory ページング対応のインベントリ
 */
```

**ポイント：**
- **バッククォート記法** - `[ClassName]` でリンクが生成される
- **完全修飾名** - 必要に応じて `package.ClassName` で記述
- **説明** - 関連性を「何との関連か」で説明を付ける

---

## メソッド・関数 KDoc

### 基本構造

```kotlin
/**
 * [1行での目的]
 *
 * [詳細説明（複雑な場合）]
 *
 * @param [パラメータ名] [説明]
 * @return [戻り値の説明]
 * @throws [例外型] [何のときに投げられるか]
 */
fun methodName(param: Type): ResultType {
    // ...
}
```

### 1行目

- **現在形・能動態** で説明
- **何を返すか** を簡潔に述べる

**よい例：**
```kotlin
/**
 * インベントリのコンテンツをレンダリングします。
 */
abstract fun renderContents()

/**
 * インベントリをプレイヤーに開きます。
 */
fun open(player: Player)

/**
 * Bukkit の Inventory オブジェクトを取得します。
 */
override fun getInventory(): Inventory
```

**悪い例：**
```kotlin
/**
 * レンダリング
 */
abstract fun renderContents()

/**
 * Opens an inventory
 */
fun open(player: Player)
```

### 詳細説明

複雑な処理の場合のみ追加：

```kotlin
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
 */
fun render()
```

### パラメータ（@param）

- **1パラメータ1行**
- **Nullable 型** - 「受け取らない場合は ... 」と記述
- **関数型** - 「ラムダ形式で ...」と説明

**例：**
```kotlin
/**
 * 複数のスロットに ItemStack を設定し、[MInventoryItem] デコレータで設定します。
 *
 * @param slots 設定対象のスロット番号の配列
 * @param itemStack 設定する ItemStack
 * @param init [MInventoryItem] の初期化ラムダ（デフォルト：空）
 */
fun set(
    slots: IntArray,
    itemStack: ItemStack,
    init: MInventoryItem.() -> Unit = {}
)
```

### 戻り値（@return）

```kotlin
/**
 * Bukkit の Inventory オブジェクトを取得します。
 *
 * @return このホルダーが管理する Inventory
 */
override fun getInventory(): Inventory
```

**説明の形式：**
- **単純な場合** - 「〜を返します」と説明
- **複雑な場合** - 「〜を返す。〜の場合は ...」と条件を付ける

**例：**
```kotlin
/**
 * ユーザーコマンドを処理し、実行結果を返します。
 *
 * @return コマンド実行の成功時は `0`、失敗時は非ゼロを返します。
 */
```

### 例外（@throws）

```kotlin
/**
 * インベントリのコンテンツをレンダリングしてプレイヤーに開きます。
 *
 * @param player インベントリを開くプレイヤー
 * @throws IllegalArgumentException プレイヤーがオンラインでない場合
 */
fun open(player: Player)
```

---

## プロパティ KDoc

### 基本構造

```kotlin
/**
 * [説明]
 */
val propertyName: Type
```

### 単一行説明

- **簡潔に** - プロパティの意味を説明
- **初期値情報** - 必要に応じて

**例：**
```kotlin
/**
 * インベントリ内のアイテムをスロット番号でマッピング。スレッドセーフです。
 */
val items = ConcurrentHashMap<Int, MInventoryItem>()

/**
 * インベントリクローズ時に実行されるコールバックのリスト。
 */
val onClose: MutableList<InventoryCloseContext.() -> Unit> = mutableListOf()

/**
 * `true` の場合、[set] メソッドでアイテムを設定した時点でインベントリに反映されます。
 * `false` の場合は [render] メソッド実行時に反映されます。
 *
 * デフォルトは `false` です。
 */
open val renderOnSet = false
```

---

## エッジケースと例外

### null 安全性

```kotlin
/**
 * コンフィグをキーで取得します。
 *
 * @param key 取得するキー
 * @return 値が存在する場合は値を、存在しない場合は `null` を返します。
 */
fun getConfig(key: String): String? = configs[key]
```

### リソース管理

```kotlin
/**
 * ファイルを開いて処理を実行します。
 *
 * このメソッドはファイルを自動的にクローズします。
 *
 * @param path ファイルパス
 * @param block ファイルハンドルを受け取るラムダ
 */
fun withFile(path: String, block: (File) -> Unit)
```

### オーバーロード

```kotlin
/**
 * String で タイトルを指定するコンストラクタの代替。
 *
 * 内部的には [Component.text] で [Component] に変換されます。
 *
 * @param title インベントリの表示名（String）
 * @param row インベントリの行数（1～6 の値）
 * @see MInventory(Component, Int) Component 版のコンストラクタ
 */
constructor(
    title: String,
    row: @Range(from = 1, to = 6) Int
): this(Component.text(title), row)
```

---

## 言語・スタイル

### 言語

- **Man10Library は日本語 KDoc で統一**
- **新規追加時も日本語を使用**
- **公式ライブラリの型名は英語のまま** - ただし `[Component]` のようにバッククォート記法で参照

### 記号・マークアップ

| 目的 | KDoc記法 | 例 |
|------|-----------|-----|
| 型・クラス参照 | `[ClassName]` | [Component]、[MInventoryItem] |
| 外部リンク | {@link package.ClassName} | {@link Commands} |
| メソッド参照 | `[methodName]` | [set]、[renderContents] |
| 強調 | `**テキスト**` | `**重要**` |
| コード | ``` `\`\`\`kotlin ... \`\`\`` ``` | コード例参照 |

### 句読点

- **句点** - 段落の終わりに「。」を付ける
- **句読点の繰り返し** - 不要な句点は避ける
- **リスト項目** - 各項目が完全文の場合のみ「。」を付ける

**例：**
```kotlin
/**
 * このクラスは以下の機能を提供します：
 *
 * - アイテム配置の管理
 * - イベントハンドリング
 * - 自動レンダリング
 *
 * 詳細は【セクション】を参照してください。
 */
```

---

## チェックリスト

スキルを公開する前に確認：

- [ ] 1行目は動詞で始まる現在形か？
- [ ] パラメータすべてに @param がついているか？
- [ ] 戻り値のある関数に @return がついているか？
- [ ] 例外を投げる関数に @throws がついているか？
- [ ] 関連クラス・メソッドへの @see リンクは十分か？
- [ ] コード例が正確で実行可能か？
- [ ] 日本語で統一されているか？（型名は除く）
- [ ] マークアップ（`` [ ] ``、``` ``` ```）は正しいか？

