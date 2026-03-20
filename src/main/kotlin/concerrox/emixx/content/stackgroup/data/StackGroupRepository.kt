package concerrox.emixx.content.stackgroup.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.config.EmiPlusPlusPaths
import concerrox.emixx.content.stackgroup.data.group.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.upgrader.LegacyStackGroupUpgrader
import concerrox.emixx.util.logDebug
import concerrox.emixx.util.logInfo
import concerrox.emixx.util.logWarn
import concerrox.emixx.util.logWarnException
import dev.emi.emi.runtime.EmiReloadLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.readText

class StackGroupRepository {

    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    suspend fun loadStackGroups() = withContext(Dispatchers.IO) {
        val stackGroups = mutableListOf<EmiStackGroupV2>()
        logInfo("Loading stack groups…")

        if (EmiPlusPlusPaths.STACK_GROUPS.notExists()) {
            logInfo("Stack group directory does not exist, creating…")
            EmiPlusPlusPaths.STACK_GROUPS.createDirectories()
        }

        for (path in EmiPlusPlusPaths.STACK_GROUPS.listDirectoryEntries()) {
            if (path.extension != "json") {
                logWarn("Invalid stack group file type: ${path.fileName}, expected JSON, skipping…")
                continue
            }

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

}