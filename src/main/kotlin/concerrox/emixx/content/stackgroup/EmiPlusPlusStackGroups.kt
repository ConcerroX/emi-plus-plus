package concerrox.emixx.content.stackgroup

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.BakedEmiStackGrouper
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.EmiStackGrouper
import concerrox.emixx.util.logError
import concerrox.emixx.util.logInfo
import dev.emi.emi.registry.EmiStackList
import kotlin.io.path.*

object EmiPlusPlusStackGroups {

    private val STACK_GROUP_DIRECTORY_PATH = EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "groups"

    val stackGroups = mutableListOf<EmiStackGroupV2>()
    val enabledStackGroups = mutableListOf<EmiStackGroupV2>()
    lateinit var indexStackGrouper: BakedEmiStackGrouper

    @JvmStatic
    fun clear() {
        stackGroups.clear()
        enabledStackGroups.clear()
    }

    @JvmStatic
    fun reload() {
        load()
        bake()
    }

    fun load() {
        logInfo("Loading stack groups…")
        STACK_GROUP_DIRECTORY_PATH.createDirectories().listDirectoryEntries("*.json").forEach {
            EmiStackGroupV2.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(it.readText())).ifSuccess { ret ->
                stackGroups += ret
                if (ret.isEnabled) enabledStackGroups += ret
            }.ifError { err ->
                logError("Failed to load stack group ${it.fileName}: ${err.message()}")
            }
        }
    }

    fun bake() {
        logInfo("Baking stack groups…")
        indexStackGrouper = EmiStackGrouper(EmiStackList.filteredStacks, enabledStackGroups).bake()
    }

    private fun getFilePath(stackGroup: EmiStackGroupV2) = STACK_GROUP_DIRECTORY_PATH / stackGroup.configFilename

    fun create(stackGroup: EmiStackGroupV2) {
        EmiStackGroupV2.CODEC.encodeStart(JsonOps.INSTANCE, stackGroup).ifSuccess {
            getFilePath(stackGroup).createFile().writeText(it.toString())
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