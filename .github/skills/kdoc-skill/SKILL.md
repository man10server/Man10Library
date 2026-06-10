---
name: kdoc-skill
description: >
  Kotlin プロジェクトの KDoc（Kotlin Documentation）を作成・管理・生成・品質チェックするスキル。
  ユーザーが「KDocを書いて」「ドキュメントを生成して」「KDocの品質をチェック」「Dokkaを設定して」
  などと言った場合は必ずこのスキルを使うこと。KDocコメント作成、品質チェック、Dokka統合まで
  一貫してサポートする。Man10Library プロジェクトのKDocベストプラクティスに準拠。
---

# Kotlin KDoc スキル

Kotlin プロジェクトの KDoc（Kotlin Documentation）を効果的に作成・管理するためのスキル。
Man10Library プロジェクトの高品質なKDocを参考にしながら、新しいクラスやメソッドのドキュメンテーションを実装します。

---

## 概要

KDoc はKotlin公式のドキュメンテーションコメント形式で、Dokka などのツールで HTML ドキュメントに変換できます。
このスキルは以下を提供します：

1. **KDoc の作成** - クラス、メソッド、プロパティへの適切なドキュメンテーション
2. **品質チェック** - パラメータ、戻り値、例外の文書化漏れを検出
3. **Dokka 統合** - 自動ドキュメント生成のセットアップ
4. **ベストプラクティス** - Man10Library 準拠のスタイルガイド

---

## 手順

### ステップ 1：KDoc の作成または修正

ユーザーが「KDoc を書いて」「コメントを追加して」などと依頼した場合、以下を確認します：

1. **対象ファイルを特定する**
   - `src/main/kotlin/` 配下の対象ファイルを特定
   - 既に KDoc がある場合は修正対象を整理

2. **references/kdoc-standards.md を参照して標準に合わせる**
   - クラス KDoc には概要、使用例、パラメータ説明を含める
   - メソッド KDoc にはパラメータ、戻り値、例外の説明を含める
   - `@see` で関連型・メソッドへのリンクを設定
   - code block にはコード例を含める（``\`kotlin\`` でハイライト）

3. **KDoc を追加する**
   - クラス定義の直前に `/**` で始まるコメントを記述
   - メソッド・プロパティ定義の直前に同様にコメント
   - パラメータは `@param` で、戻り値は `@return` で説明
   - @link で型の参照を記述するときは `[ClassName]` 形式を使用

### ステップ 2：KDoc 品質チェック

ユーザーが「KDoc の品質をチェック」「ドキュメントの漏れを確認」などと依頼した場合：

1. **references/kdoc-quality-checklist.md を使用**
   - 各クラス・メソッドに対してチェックリストを実施
   - 以下が文書化されているか確認：
     - クラス：概要、使用例、パラメータ（コンストラクタ引数）
     - メソッド：概要、パラメータ、戻り値、例外
     - プロパティ：説明

2. **不足がある場合は修正案を提示**
   - どの型・メソッドのドキュメントが不足しているか列挙
   - 修正案を `@param`, `@return`, `@throws` 形式で提示

3. **スクリーンショット / レポート化**
   - 必要に応じて品質スコアをレポート（例：80% のメソッドがドキュメント化済）

### ステップ 3：Dokka による自動ドキュメント生成

ユーザーが「ドキュメントを生成して」「Dokka を設定して」などと依頼した場合：

1. **references/dokka-integration.md を参照**
   - プロジェクトが Gradle を使用していることを確認
   - Dokka プラグイン設定を `build.gradle.kts` に追加（既になければ）

2. **生成コマンドを実行**
   ```bash
   ./gradlew dokkaHtml
   ```
   - または、IDEの Gradle タスク UI から `dokkaHtml` を実行

3. **出力ドキュメントの確認**
   - `build/dokka/html/` に生成されたファイルを確認
   - ブラウザで `index.html` を開いて UI を確認

4. **生成後の確認事項**
   - すべてのクラス・メソッドが表示されているか
   - コード例が正しくレンダリングされているか
   - リンク（@see）が機能しているか

---

## 例：MInventory クラスへの KDoc 追加

Man10Library の `MInventory` クラスは、以下の特徴を持つ高品質な KDoc の例です：

- **概要部分** - クラスの目的と使用シーン を簡潔に説明
- **基本的な使用例** - コード例を含めた実装パターン
- **サブセクション** - アイテム設定方法、イベントハンドリングごとに分けて説明
- **パラメータ説明** - コンストラクタ引数の詳細を記述
- **関連クラス参照** - `@see` でリンク

```kotlin
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
 *
 * @param title インベントリの表示名（[Component] 形式）
 * @param row インベントリの行数（1～6 の値）
 *
 * @see InventoryClickContext
 * @see MInventoryItem
 */
abstract class MInventory(
    title: Component,
    row: @Range(from = 1, to = 6) Int
): InventoryHolder {
    // ...
}
```

---

## 注意事項

- **コード例** - KDoc 内のコード例は実装が正確である必要があります。コンパイルテストを推奨
- **パラメータ名** - `@param` の説明では、実際のパラメータ名と一致させる
- **戻り値の型** - `@return` には、戻り値の型と「どんなときに何を返すか」を明記
- **破壊的変更** - API 変更時は既存 KDoc も一緒に更新
- **言語** - Man10Library は日本語KDoc で統一。新規追加時も日本語を使用
- **Dokka 出力** - MiniMessage や Component などのライブラリ型への `@link` は型安全性が保証される

---

## 参考資料

- **kdoc-standards.md** - Man10Library のKDocベストプラクティス詳細版
- **kdoc-quality-checklist.md** - KDoc品質チェック項目と基準
- **dokka-integration.md** - Dokka セットアップと統合方法
- **Kotlin 公式ドキュメント** - https://kotlinlang.org/docs/kotlin-doc.html
- **Dokka 公式** - https://kotlinlang.org/docs/dokka-introduction.html

