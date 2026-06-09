package red.man10.man10library.config

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.configuration.file.YamlConfiguration
import red.man10.man10library.MJavaPlugin
import java.io.File

@Suppress("unused")
object MConfig {

    private val plugin by lazy { MJavaPlugin.plugin }

    suspend fun getConfig(path: String): YamlConfiguration? {
        return withContext(plugin.asyncDispatcher) {
            val file = File(plugin.dataFolder, path)
            if (!file.exists()) {
                return@withContext null
            }
            return@withContext YamlConfiguration.loadConfiguration(file)
        }
    }

    suspend fun saveConfig(config: YamlConfiguration, path: String) {
        withContext(plugin.asyncDispatcher) {
            val file = File(plugin.dataFolder, path)
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            config.save(file)
        }
    }

    suspend fun getConfigsRecursively(folder: String): List<YamlConfiguration> {
        return withContext(plugin.asyncDispatcher) {
            val directory = File(plugin.dataFolder, folder)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext emptyList()
            }
            val configs = mutableListOf<YamlConfiguration>()
            directory.walkTopDown().forEach { file ->
                if (file.isFile && file.extension == "yml") {
                    configs.add(YamlConfiguration.loadConfiguration(file))
                }
            }
            return@withContext configs
        }
    }
}