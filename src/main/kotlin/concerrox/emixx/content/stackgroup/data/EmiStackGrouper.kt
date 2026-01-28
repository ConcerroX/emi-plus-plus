package concerrox.emixx.content.stackgroup.data

import concerrox.emixx.content.stackgroup.stack.EmiGroupStack
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import concerrox.emixx.util.logDebugTime
import dev.emi.emi.api.stack.EmiStack
import java.util.concurrent.ConcurrentHashMap

data class EmiStackGrouper(val stackSource: List<EmiStack>, val stackGroups: List<EmiStackGroupV2>) {

    fun bake() = logDebugTime("Baked a stack grouper in {}") {

        // Those collections are large enough so we don’t need to resize
        val preGroupedStackList = ArrayList<EmiStack>(stackSource.size)
        val stackToStackGroups = ConcurrentHashMap<EmiStack, MutableList<EmiStackGroupV2>>(stackSource.size)

        stackGroups.parallelStream().forEach { group ->
            for (stack in stackSource) {
                if (!group.match(stack)) continue
                stackToStackGroups.getOrPut(stack) { mutableListOf() }.add(group)
            }
        }

        val stackGroupToGroupStackContents = mutableMapOf<EmiStackGroupV2, MutableList<GroupedEmiStackWrapper<EmiStack>>>()
        for (stack in stackSource) {
            val stackGroups = stackToStackGroups[stack]
            if (stackGroups == null) {
                preGroupedStackList.add(stack)
                continue
            }
            for (stackGroup in stackGroups) {
                val contents = stackGroupToGroupStackContents.getOrPut(stackGroup) {
                    val list = mutableListOf<GroupedEmiStackWrapper<EmiStack>>()
                    preGroupedStackList += EmiGroupStack(stackGroup, list)
                    return@getOrPut list
                }
                contents += GroupedEmiStackWrapper(stack, stackGroup)
            }
        }

        // We don’t need to freeze these collections as they’re in immutable types in BakedStackGrouper
        return@logDebugTime BakedEmiStackGrouper(stackSource, stackGroups, preGroupedStackList, stackToStackGroups)
    }

}