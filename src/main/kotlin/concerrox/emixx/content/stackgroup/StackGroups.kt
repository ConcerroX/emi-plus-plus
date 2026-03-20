package concerrox.emixx.content.stackgroup

import com.google.gson.GsonBuilder
import com.mojang.serialization.JsonOps
import concerrox.emixx.config.EmiPlusPlusPaths
import concerrox.emixx.content.stackgroup.data.StackGroupRepository
import concerrox.emixx.content.stackgroup.data.group.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.grouper.BakedEmiStackGrouper
import concerrox.emixx.content.stackgroup.data.grouper.EmiStackGrouper
import concerrox.emixx.util.logError
import concerrox.emixx.util.logInfo
import dev.emi.emi.registry.EmiStackList
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.system.measureTimeMillis

object StackGroups {

    private val STACK_GROUP_DIRECTORY = EmiPlusPlusPaths.STACK_GROUPS
    private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    val stackGroups = mutableListOf<EmiStackGroupV2>()
    val enabledStackGroups = mutableListOf<EmiStackGroupV2>()

    var indexStackGrouper: BakedEmiStackGrouper? = null

    suspend fun reload() {
        logInfo("Reloading stack groups…")
        load()
        bake()
    }

    suspend fun load() {
        val time = measureTimeMillis {
            stackGroups += StackGroupRepository().loadStackGroups()
            enabledStackGroups += stackGroups.filter { it.isEnabled }
        }
        logInfo("Loaded ${stackGroups.size} stack groups (${enabledStackGroups.size} enabled) in $time ms.")
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