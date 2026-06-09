package red.man10.man10library.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.ConfigurationSection
import red.man10.man10library.MJavaPlugin

@Suppress("unused")
object MHikariDataSourceBuilder {

    private val types = listOf(DatabaseType.MySQL, DatabaseType.PostgreSQL, DatabaseType.SQLite)

    fun build(): HikariDataSource {
        val pluginConfig = MJavaPlugin.plugin.config
        val database = pluginConfig.getConfigurationSection("database") ?: throw IllegalArgumentException("Missing 'database' configuration section")
        return build(database)
    }

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

    internal sealed class DatabaseType(val name: String) {
        abstract fun buildJdbcUrl(section: ConfigurationSection): String

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