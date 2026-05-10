# Man10Library

[![Java Version](https://img.shields.io/badge/Java-21-brightgreen)](https://www.oracle.com/java/)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.4.0--Beta2-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

Man10Library は、PaperMC プラグイン開発用の Kotlin ライブラリです。DSL マーカーを使用した型安全なコマンド、イベント、インベントリシステムを提供します。

## 特徴

- 🎯 **型安全な DSL**: Kotlin の DSL マーカーを使用した直感的な API
- ⚡ **コマンドビルダー**: Brigadier ベースの強力なコマンドシステム
- 📦 **イベント管理**: 簡潔なイベントハンドリング
- 🎨 **インベントリシステム**: 複雑なインベントリUI構築に対応
- 🔧 **アイテムスタック DSL**: ItemStack の構築を簡素化

## セットアップ

### Gradle での使用

`build.gradle.kts` に以下を追加:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/man10server/Man10Library")
        credentials {
            username = System.getenv("GITHUB_USERNAME") ?: project.findProperty("gpr.user").toString()
            password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key").toString()
        }
    }
}

dependencies {
    implementation("red.man10:man10-library:1.0.0")
}
```

## プロジェクト構成

```
src/main/kotlin/red/man10/man10library/
├── MJavaPlugin.kt                 # メインプラグインクラス
├── command/                       # コマンドシステム
│   ├── MCommand.kt
│   ├── MCommandBody.kt
│   ├── MCommandData.kt
│   ├── MCommandObject.kt
│   └── argument/
├── event/                         # イベントシステム
│   ├── MEvent.kt
│   └── MEventUnit.kt
├── inventory/                     # インベントリシステム
│   ├── MInventory.kt
│   ├── builtin/
│   ├── context/
│   └── itemStack/
├── dslMarker/                     # DSL マーカー
└── utils/
```

## パッケージング

本ライブラリは Shadow JAR を使用して、依存関係をシェーディングします。

```bash
./gradlew shadowJar
```

生成されたファイル: `build/libs/Man10Library-all.jar`

## 公開

GitHub に新しいバージョンをタグで公開すると、自動的に GitHub Packages に公開されます。

```bash
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1
```

詳細は [.github/workflows/publish.yml](.github/workflows/publish.yml) を参照してください。

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。詳細は [LICENSE](LICENSE) を参照してください。


**Note**: このプロジェクトは PaperMC サーバーで使用する Kotlin プラグインライブラリです。

