# Dokka 統合ガイド

Dokka は Kotlin の公式ドキュメント生成ツール。KDoc から HTML ドキュメントを自動生成します。

---

## 目次

1. [Dokka とは](#dokka-とは)
2. [セットアップ](#セットアップ)
3. [ドキュメント生成](#ドキュメント生成)
4. [設定・カスタマイズ](#設定カスタマイズ)
5. [トラブルシューティング](#トラブルシューティング)

---

## Dokka とは

### 概要

- **Dokka** は Kotlin の公式ドキュメント生成ツール
- KDoc（コメント）から HTML/Markdown ドキュメントを生成
- マルチプラットフォーム対応（JVM、JS、Native）
- **Gradle プラグイン** として統合可能

### メリット

- IDE 内のドキュメントで見たのと同じ形式で HTML が生成される
- KDoc（/** ... */）をコードの近くに書ける
- バージョン管理が容易（ドキュメントとコード の同期）
- 自動生成のため更新漏れがない

---

## セットアップ

### 前提条件

- Gradle 6.1 以上
- Kotlin 1.4 以上
- Java 8 以上

Man10Library は既に以下を使用：
- Gradle 8.x（build.gradle.kts 使用）
- Kotlin 2.x

### 1. build.gradle.kts に Dokka プラグインを追加

`build.gradle.kts` の `plugins` セクションに以下を追加：

```kotlin
plugins {
    // ... 既存のプラグイン ...
    id("org.jetbrains.dokka") version "1.9.20"  // 最新版を使用
}
```

Man10Library の現在の build.gradle.kts：

```kotlin
plugins {
    kotlin("kapt") version "2.0.0"
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    // ここに追加：
    id("org.jetbrains.dokka") version "1.9.20"
}
```

### 2. Dokka タスクが自動で利用可能に

プラグイン追加後は、以下のタスクが自動で使用可能になります：

- `dokkaHtml` - HTML 形式のドキュメント生成（最も一般的）
- `dokkaMarkdown` - Markdown 形式
- `dokkaJavadoc` - JavaDoc 형식（Java との互換性用）

---

## ドキュメント生成

### コマンドラインから実行

```bash
# HTML ドキュメント生成
./gradlew dokkaHtml

# Gradle タスク UI から実行（IDE）
# 1. Gradle サイドバー → Tasks → documentation → dokkaHtml
# 2. ダブルクリック
```

### 出力場所

生成されたドキュメントは以下に出力：

```
build/dokka/html/
├── index.html              # トップページ
├── red/man10/man10library/
│   ├── index.html
│   ├── -m-java-plugin/index.html
│   ├── command/
│   │   ├── index.html
│   │   ├── -m-command/index.html
│   │   └── ...
│   ├── inventory/
│   │   ├── index.html
│   │   └── ...
│   └── ...
└── styles.css              # スタイルシート
```

### ドキュメントの確認

1. **ブラウザで確認**
   ```bash
   # Windows
   start build/dokka/html/index.html
   
   # Mac
   open build/dokka/html/index.html
   
   # Linux
   xdg-open build/dokka/html/index.html
   ```

2. **IDE での確認**
   - IntelliJ IDEA：Build → Documentation → Dokka... → Open in Browser

3. **確認すべき項目**
   - [ ] すべてのクラス・メソッドが表示されているか
   - [ ] コード例が正しくレンダリングされているか
   - [ ] リンク（@see）が正常に機能するか
   - [ ] 日本語が正しく表示されるか

---

## 設定・カスタマイズ

### デフォルト設定での生成

Dokka プラグイン追加後、デフォルト設定で `dokkaHtml` タスク実行するだけで OK。

### 詳細設定（オプション）

より細かく設定したい場合、`build.gradle.kts` に以下を記述：

```kotlin
dokka {
    dokkaPublications.html {
        // サイト名
        homepageLink.set("https://github.com/panshare/Man10Library")
        
        // モジュール名
        moduleName.set("Man10Library")
        
        // ドキュメント出力先
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}
```

### 外部ドキュメントリンク（オプション）

Bukkit や Paper などの外部ライブラリをリンクする場合：

```kotlin
dokka {
    dokkaPublications.html {
        externalDocumentationLinks.create("bukkit") {
            url.set(URI("https://hub.spigotmc.org/javadocs/bukkit/").toURL())
            packageListUrl.set(URI("https://hub.spigotmc.org/javadocs/bukkit/element-list").toURL())
        }
    }
}
```

---

## CI/CD との統合

### GitHub Actions での自動生成（推奨）

`.github/workflows/dokka.yml` を作成：

```yaml
name: Generate Dokka Documentation

on:
  push:
    branches:
      - main
  pull_request:

jobs:
  dokka:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build Dokka HTML
        run: ./gradlew dokkaHtml
      
      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: dokka-html
          path: build/dokka/html/
          
      # オプション：GitHub Pages にデプロイ
      - name: Deploy to GitHub Pages
        if: github.ref == 'refs/heads/main'
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build/dokka/html
```

### 手動実行（プッシュ前チェック）

```bash
# ローカルで生成してブラウザで確認
./gradlew dokkaHtml
open build/dokka/html/index.html

# 問題なければコミット・プッシュ
git add ...
git commit -m "docs: add KDoc for XXX"
git push
```

---

## トラブルシューティング

### 問1：日本語が文字化けする

**原因** - 出力エンコーディングが UTF-8 でない

**解決法** - `build.gradle.kts` に以下を追加：

```kotlin
tasks.dokkaHtml {
    dokkaSourceSets {
        named("main") {
            // エンコーディング明示
        }
    }
}
```

または、JVM オプション：

```bash
./gradlew dokkaHtml -Dfile.encoding=UTF-8
```

### 問2：外部ライブラリへのリンクが壊れている

**原因** - `@see [ExternalClass]` の参照が解決されていない

**解決法** - external documentation link を設定（上記参照）

### 問3："Couldn't find description for … parameter"

**原因** - パラメータとコメント内の @param 名が一致していない

**確認・修正**：

```kotlin
// 悪い例
fun set(slot: Int) { }
/**
 * @param index スロット番号  // ❌ パラメータ名が slot なのに index と記述
 */

// 良い例
fun set(slot: Int) { }
/**
 * @param slot スロット番号  // ✅ 一致
 */
```

### 問4：ビルド失敗（"Dokka failed"）

```bash
# ビルド実行時にエラーメッセージを確認
./gradlew dokkaHtml --stacktrace

# キャッシュをクリア
./gradlew clean dokkaHtml
```

---

## ベストプラクティス

### 1. 定期的に生成・確認

```bash
# 週に 1 回または PR マージ前に確認
./gradlew dokkaHtml
# ブラウザで確認
```

### 2. CI で自動生成

GitHub Actions で毎回自動生成させれば、更新漏れを防げます。

### 3. KDoc 品質チェック

生成前に `kdoc-quality-checklist.md` でチェック。

### 4. GitHub Pages にデプロイ

CI 連携で main ブランチにプッシュ → GitHub Pages に自動デプロイ。

```
https://username.github.io/Man10Library/
```

で公開可能。

---

## 参考資料

- **Dokka 公式** - https://kotlinlang.org/docs/dokka-introduction.html
- **Dokka GitHub** - https://github.com/Kotlin/dokka
- **KDoc 公式** - https://kotlinlang.org/docs/kotlin-doc.html

