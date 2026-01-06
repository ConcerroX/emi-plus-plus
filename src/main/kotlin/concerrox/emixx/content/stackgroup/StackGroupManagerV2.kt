package concerrox.emixx.content.stackgroup

import com.google.gson.JsonParser
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

object StackGroupManagerV2 {

    private val STACK_GROUP_DIRECTORY_PATH = EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "groups"

    private val appScope = CoroutineScope(Dispatchers.Default)
    private val stackGroups = mutableListOf<EmiStackGroupV2>()
    private val enabledStackGroups = mutableListOf<EmiStackGroupV2>()
    private val stackToGroupCache = mutableMapOf<EmiStack, MutableList<EmiStackGroupV2>>()

    fun reload() {
        readStackGroups()
        // TODO: register some new comparator for flags and so on
        // TODO: read from cache
        appScope.launch {
            stackToGroupCache.clear()
            collectContentsAndSaveToCache(enabledStackGroups)
        }
    }

    private fun readStackGroups() {
        stackGroups.clear()
        STACK_GROUP_DIRECTORY_PATH.createDirectories().listDirectoryEntries("*.json").forEach {
            val result = EmiStackGroupV2.parse(JsonParser.parseString(it.readText()), it.fileName)
            if (result != null) stackGroups += result
        }
        enabledStackGroups.clear()
        enabledStackGroups.addAll(stackGroups.filter { it.isEnabled })
    }

    fun collectContents(groups: List<EmiStackGroupV2>): List<EmiStackGroupV2> {
        return StackGroupContentCollector.collect(EmiStackList.filteredStacks, groups)
    }

    fun collectAllContentsAsync(callback: (List<EmiStackGroupV2>) -> Unit = {}) {
        appScope.launch {
            callback(collectContents(stackGroups))
        }
    }

    fun collectContentsAndSaveToCache(groups: List<EmiStackGroupV2>) {
        StackGroupContentCollector.collect(EmiStackList.filteredStacks, groups)
        for (group in groups) {
            if (group.collectedStacks.isEmpty()) continue
            for (stack in group.collectedStacks) {
                stackToGroupCache.getOrPut(stack) { mutableListOf() }.add(group)
            }
        }
    }

}