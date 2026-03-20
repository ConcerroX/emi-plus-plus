package concerrox.emixx.content.stackgroup

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.BakedEmiStackGrouper
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.EmiStackGrouper
import concerrox.emixx.content.stackgroup.data.LegacyStackGroupUpgrader
import concerrox.emixx.util.logError
import concerrox.emixx.util.logInfo
import dev.emi.emi.registry.EmiStackList
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText

object StackGroups {

    private val STACK_GROUP_DIRECTORY = EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "groups"
    private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    val stackGroups = mutableListOf<EmiStackGroupV2>()
    val enabledStackGroups = mutableListOf<EmiStackGroupV2>()
    var indexStackGrouper: BakedEmiStackGrouper? = null

    fun reload() {
        load()
        bake()
    }

    fun load() {
        logInfo("Loading stack groups…")
        for (path in STACK_GROUP_DIRECTORY.createDirectories().listDirectoryEntries("*.json")) {
            var json = JsonParser.parseString(path.readText())

            // Upgrade legacy stack groups
            if (LegacyStackGroupUpgrader.isLegacy(json)) json = LegacyStackGroupUpgrader.upgrade(GSON, json, path)

            EmiStackGroupV2.CODEC.parse(JsonOps.INSTANCE, json).ifSuccess {
                if (it.isEnabled) enabledStackGroups += it
                stackGroups += it
            }.ifError { err ->
                logError("Failed to load stack group ${path.fileName}: ${err.message()}")
            }
        }
    }

    fun bake() {
        logInfo("Baking stack groups…")
        indexStackGrouper = EmiStackGrouper(EmiStackList.filteredStacks, enabledStackGroups).bake()
    }

    fun clear() {
        stackGroups.clear()
        enabledStackGroups.clear()
    }


    private fun getFilePath(stackGroup: EmiStackGroupV2) = STACK_GROUP_DIRECTORY / stackGroup.configFilename

    fun create(stackGroup: EmiStackGroupV2) {
        EmiStackGroupV2.CODEC.encodeStart(JsonOps.INSTANCE, stackGroup).ifSuccess {
            getFilePath(stackGroup).createFile().writeText(GSON.toJson(it))
        }.ifError {
            logError("Failed to save stack group ${stackGroup.id}: ${it.message()}")
        }
        stackGroups += stackGroup
        if (stackGroup.isEnabled) enabledStackGroups += stackGroup
    }

    fun update(old: EmiStackGroupV2, new: EmiStackGroupV2) {
        stackGroups.remove(old)
        enabledStackGroups.remove(old)
        getFilePath(old).deleteExisting()
        create(new)
    }

}