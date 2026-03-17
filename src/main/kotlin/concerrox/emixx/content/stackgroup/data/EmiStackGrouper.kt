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
        val stackGroupToContent = mutableMapOf<EmiStackGroupV2, MutableList<GroupedEmiStackWrapper<EmiStack>>>()

        stackGroups.parallelStream().forEach { group ->
            for (stack in stackSource) {
                if (group.match(stack)) {9
                    // TODO: bake match method
                    stackToStackGroups.getOrPut(stack) { mutableListOf() }.add(group)
                }
            }
        }

        for (stack in stackSource) {
            val stackGroups = stackToStackGroups[stack]

            // When not in any group
            if (stackGroups == null) {
                preGroupedStackList += stack
                continue
            }

            for (group in stackGroups) {
                var content = stackGroupToContent[group]

                // Initialize the content if not exist
                if (content == null) {
                    content = mutableListOf()
                    stackGroupToContent[group] = content
                    preGroupedStackList += EmiGroupStack(group, content)
                }

                // Wrap the stack and add it to the content
                content += GroupedEmiStackWrapper(stack, group)
            }
        }

        // We don’t need to freeze these collections as they’re in immutable types in BakedStackGrouper
        BakedEmiStackGrouper(stackSource, stackGroups, preGroupedStackList, stackToStackGroups)
    }

}