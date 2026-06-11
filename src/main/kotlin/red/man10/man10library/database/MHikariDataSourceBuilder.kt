package red.man10.man10library.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.ConfigurationSection
import red.man10.man10library.MJavaPlugin

/**
 * `config.yml` の database 設定から [HikariDataSource] を構築するビルダーユーティリティ。
 *
 * MySQL / PostgreSQL / SQLite に対応しており、`jdbcUrl` を直接指定する方法と、
 * `type` や接続先情報から JDBC URL を組み立てる方法の両方をサポートします。
 *
 * ### 使用例
 *
 * ```yaml
 * database:
 *   type: mysql
 *   host: localhost
 *   port: 3306
 *   database: mydb
 *   username: root
 *   password: secret
 *   maximumPoolSize: 10
 * ```
 *
 * ```kotlin
 * val dataSource = MHikariDataSourceBuilder.build()
 * ```
 *
 * ### 対応設定
 *
 * - `type` - `mysql` / `postgresql` / `sqlite`
 * - `jdbcUrl` - 明示的な JDBC URL。指定時は `type` による自動生成より優先
 * - `host` / `port` / `database` / `params` - URL 自動生成用
 * - `username` / `password` - 接続認証情報
 * - `poolName` - HikariCP のプール名
 * - `connectionTimeout` / `validationTimeout` / `idleTimeout` / `leakDetectionThreshold` / `maxLifetime`
 * - `maximumPoolSize` / `minimumIdle`
 *
 * ### 注意点
 *
 * `build()` は `MJavaPlugin.plugin.config` から `database` セクションを取得します。
 * セクションが存在しない場合は例外を投げます。
 */
@Suppress("unused")
object MHikariDataSourceBuilder {

    private val types = listOf(DatabaseType.MySQL, DatabaseType.PostgreSQL, DatabaseType.SQLite)

    /**
     * プラグイン設定の `database` セクションから [HikariDataSource] を生成します。
     *
     * `database` セクションが存在しない場合は [IllegalArgumentException] を投げます。
     *
     * @return 構築された [HikariDataSource]
     * @throws IllegalArgumentException `database` セクションが存在しない場合
     */
    fun build(): HikariDataSource {
        val pluginConfig = MJavaPlugin.plugin.config
        val database = pluginConfig.getConfigurationSection("database") ?: throw IllegalArgumentException("Missing 'database' configuration section")
        return build(database)
    }

    /**
     * 指定された設定セクションから [HikariDataSource] を生成します。
     *
     * `jdbcUrl` が設定されている場合はそれを優先し、未設定の場合は `type` と各種接続情報から
     * DB 種別ごとの JDBC URL を組み立てます。
     *
     * @param section database 設定を含む [ConfigurationSection]
     * @return 構築された [HikariDataSource]
     */
    fun build(section: ConfigurationSection): HikariDataSource {
        val config = HikariConfig()
        val typeName = section.getString("type")?.lowercase()
        val dbType = types.firstOrNull { it.name == typeName } ?: DatabaseType.MySQL
        val jdbcUrl = section.getString("jdbcUrl") ?: dbType.buildJdbcUrl(section)
        config.jdbcUrl = jdbcUrl

        config.username = section.getString("username") ?: ""
        config.password = section.getString("password") ?: ""

        config.poolName = section.getString("poolName") ?: "${MJavaPlugin.plugin.name}-HikariPool"
        config.connectionTimeout = section.getLong("connectionTimeout", config.connectionTimeout)
        config.validationTimeout = section.getLong("validationTimeout", config.validationTimeout)
        config.idleTimeout = section.getLong("idleTimeout", config.idleTimeout)
        config.leakDetectionThreshold = section.getLong("leakDetectionThreshold", config.leakDetectionThreshold)
        config.maxLifetime = section.getLong("maxLifetime", config.maxLifetime)
        config.maximumPoolSize = section.getInt("maximumPoolSize", config.maximumPoolSize)
        config.minimumIdle = section.getInt("minimumIdle", config.minimumIdle)

        return HikariDataSource(config)
    }

    /**
     * JDBC URL の生成ルールを表す DB 種別。
     *
     * `type` の値に応じて適切な URL 形式を組み立てます。
     */
    internal sealed class DatabaseType(val name: String) {
        /**
         * 指定された設定セクションから JDBC URL を構築します。
         *
         * @param section database 設定セクション
         * @return JDBC URL
         */
        abstract fun buildJdbcUrl(section: ConfigurationSection): String

        /** MySQL 用の JDBC URL 生成ルール。 */
        object MySQL : DatabaseType("mysql") {
            override fun buildJdbcUrl(section: ConfigurationSection): String {
                val host = section.getString("host") ?: "localhost"
                val port = section.getInt("port", 3306)
                val database = section.getString("database") ?: throw IllegalArgumentException("MySQL configuration requires 'database' field")
                val params = section.getString("params")
                val base = "jdbc:mysql://$host:$port/$database"
                return if (params.isNullOrEmpty()) base else "$base?$params"
            }
        }

        /** PostgreSQL 用の JDBC URL 生成ルール。 */
        object PostgreSQL : DatabaseType("postgresql") {
            override fun buildJdbcUrl(section: ConfigurationSection): String {
                val host = section.getString("host") ?: "localhost"
                val port = section.getInt("port", 5432)
                val database = section.getString("database") ?: throw IllegalArgumentException("PostgreSQL configuration requires 'database' field")
                val params = section.getString("params")
                val base = "jdbc:postgresql://$host:$port/$database"
                return if (params.isNullOrEmpty()) base else "$base?$params"
            }
        }

        /** SQLite 用の JDBC URL 生成ルール。 */
        object SQLite : DatabaseType("sqlite") {
            override fun buildJdbcUrl(section: ConfigurationSection): String {
                val database = section.getString("database") ?: throw IllegalArgumentException("SQLite configuration requires 'database' field for file path or ':memory:'")
                val params = section.getString("params")
                val base = "jdbc:sqlite:$database"
                return if (params.isNullOrEmpty()) base else "$base?$params"
            }
        }
    }
}