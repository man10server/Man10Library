package red.man10.man10library.command

/**
 * フィールドに付与して、そのフィールドが `MCommandObject` を保持することを示すアノテーション。
 *
 * `MCommand` はリフレクションでこのアノテーションが付いているフィールドを検出し、
 * 対象の `MCommandObject` からコマンド定義を読み取って登録します。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MCommandBody
