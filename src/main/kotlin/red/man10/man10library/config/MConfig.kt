package red.man10.man10library.config

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.configuration.file.YamlConfiguration
import red.man10.man10library.MJavaPlugin
import java.io.File

/**
 * プラグインの dataFolder 配下にある YAML 設定ファイルを、非同期で読み書きするユーティリティ。
 *
 * ### 主な用途
 *
 * - 単一の YAML ファイルを読み込む
 * - YAML ファイルを保存する
 * - 指定フォルダ配下の `.yml` をまとめて読み込む
 *
 * ### 注意点
 *
 * - すべての API は `suspend` 関数です。
 * - パスはプラグインの `dataFolder` からの相対パスとして解釈されます。
 * - フォルダが存在しない、またはファイルが見つからない場合は `null` / 空リストを返します。
 */
@Suppress("unused")
object MConfig {

    private val plugin by lazy { MJavaPlugin.plugin }

    /**
     * 指定されたパスの YAML 設定を非同期で読み込みます。
     *
     * ファイルが存在しない場合は `null` を返します。
     *
     * @param path `dataFolder` からの相対パス
     * @return 読み込んだ [YamlConfiguration]、存在しない場合は `null`
     */
    suspend fun getConfig(path: String): YamlConfiguration? {
        return withContext(plugin.asyncDispatcher) {
            val file = File(plugin.dataFolder, path)
            if (!file.exists()) {
                return@withContext null
            }
            return@withContext YamlConfiguration.loadConfiguration(file)
        }
    }

    /**
     * 指定されたパスに YAML 設定を非同期で保存します。
     *
     * 親ディレクトリが存在しない場合は自動で作成します。
     * ファイルが存在しない場合は新規作成してから保存します。
     *
     * @param config 保存する [YamlConfiguration]
     * @param path `dataFolder` からの相対パス
     */
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

    /**
     * 指定フォルダ配下の `.yml` ファイルを再帰的に読み込みます。
     *
     * フォルダが存在しない場合、またはフォルダではない場合は空リストを返します。
     * サブフォルダも含めて走査し、見つかった YAML をすべて [YamlConfiguration] として読み込みます。
     *
     * @param folder `dataFolder` からの相対フォルダパス
     * @return 読み込んだ [YamlConfiguration] のリスト。条件に合うファイルがない場合は空リスト
     */
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