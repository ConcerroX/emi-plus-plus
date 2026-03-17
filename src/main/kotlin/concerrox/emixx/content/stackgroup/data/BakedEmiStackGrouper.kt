package concerrox.emixx.content.stackgroup.data

import concerrox.emixx.content.stackgroup.stack.EmiGroupStack
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import dev.emi.emi.api.stack.EmiStack

data class BakedEmiStackGrouper(
    internal val stackSource: List<EmiStack>,
    internal val stackGroups: List<EmiStackGroupV2>,
    internal val preGroupedStackList: List<EmiStack>,
    internal val stackToStackGroups: Map<EmiStack, List<EmiStackGroupV2>>
) {

    override fun toString() = "BakedEmiStackGrouper[${stackSource.size}](stackGroups=$stackGroups)"

    fun search(searchedStacks: List<EmiStack>): MutableList<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedStackGroups = mutableSetOf<EmiStackGroupV2>()
        val stackGroupToContent = stackGroups.associateWith { mutableListOf<GroupedEmiStackWrapper<EmiStack>>() }

        for (stack in searchedStacks) {
            val stackGroups = stackToStackGroups[stack]
            if (stackGroups == null) {
                result += stack
            } else { // Is in groups
                for (group in stackGroups) {
                    val content = requireNotNull(stackGroupToContent[group])
                    content += GroupedEmiStackWrapper(stack, group)

                    if (group !in addedStackGroups) { // If the group hasn't been added to list yet
                        result += EmiGroupStack(group, content)
                        addedStackGroups += group
                    }
                }
            }
        }

        return result
    }

}