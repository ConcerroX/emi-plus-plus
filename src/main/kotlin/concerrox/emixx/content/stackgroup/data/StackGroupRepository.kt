package concerrox.emixx.content.stackgroup.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.config.EmiPlusPlusPaths
import concerrox.emixx.content.stackgroup.data.group.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.upgrader.LegacyStackGroupUpgrader
import concerrox.emixx.util.logDebug
import concerrox.emixx.util.logError
import concerrox.emixx.util.logInfo
import concerrox.emixx.util.logWarn
import concerrox.emixx.util.logWarnException
import dev.emi.emi.runtime.EmiReloadLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.io.path.writeText

class StackGroupRepository {

    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val stackGroupDirectory = EmiPlusPlusPaths.STACK_GROUPS

    suspend fun loadStackGroups() = withContext(Dispatchers.IO) {
        val stackGroups = mutableListOf<EmiStackGroupV2>()
        logInfo("Loading stack groups…")

        if (stackGroupDirectory.notExists()) {
            logInfo("Stack group directory does not exist, creating…")
            stackGroupDirectory.createDirectories()
        }

        var paths = listStackGroupFiles()
        if (paths.isEmpty()) {
            logInfo("Stack group directory is empty, creating preset stack groups…")
            BuiltInStackGroupPresets.PRESETS.forEach { saveStackGroup("builtin", it) }
            paths = listStackGroupFiles() // Re-list directory
            logInfo("Created ${paths.size} preset stack groups")
        }

        for (path in paths) {
            val stackGroup = loadStackGroup(path)
            if (stackGroup != null) {
                stackGroups += stackGroup
                logDebug("Loaded stack group: ${stackGroup.id}")
            }
        }

        return@withContext stackGroups
    }

    private fun loadStackGroup(path: Path): EmiStackGroupV2? = runCatching {
        runCatching {
            JsonParser.parseString(path.readText())
        }.getOrElse {
            throw Exception("Read/parse JSON failed: ${it.message}")
        }
    }.mapCatching { json ->
        runCatching {
            if (LegacyStackGroupUpgrader.isLegacy(json)) LegacyStackGroupUpgrader.upgrade(gson, json, path) else json
        }.getOrElse {
            throw Exception("Upgrade legacy stack group failed: ${it.message}")
        }
    }.mapCatching { json ->
        EmiStackGroupV2.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow {
            Exception("Codec deserialization failed: $it")
        }
    }.onFailure { e ->
        // Log to EMI if enabled in Configuration > EMI++ > Dev
        if (EmiPlusPlusConfig.logInvalidStackGroups.get()) {
            EmiReloadLog.warn("Failed to load stack group ${path.fileName}", e)
        } else {
            logWarnException("Failed to load stack group ${path.fileName}:", e)
        }
    }.getOrNull()

    suspend fun saveStackGroup(directoryName: String, stackGroup: EmiStackGroupV2): Unit = withContext(Dispatchers.IO) {
        val dirPath = stackGroupDirectory / directoryName
        if (dirPath.notExists()) dirPath.createDirectories()

        EmiStackGroupV2.CODEC.encodeStart(JsonOps.INSTANCE, stackGroup).ifSuccess {
            (dirPath / stackGroup.configFilename).createFile().writeText(gson.toJson(it))
            logDebug("Saved stack group ${stackGroup.id} to ${stackGroup.configFilename}")
        }.ifError {
            logError("Failed to save stack group ${stackGroup.id}: ${it.message()}")
        }
    }

    private suspend fun listStackGroupFiles() = withContext(Dispatchers.IO) {
        stackGroupDirectory.walk().filter {
            val isJson = it.extension == "json"
            if (!isJson) logWarn("Invalid stack group file type: ${it.fileName}, expected JSON, skipping…")
            isJson
        }.toList()
    }

}