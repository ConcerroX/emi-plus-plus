package concerrox.emixx.content.stackgroup.data

import concerrox.emixx.content.stackgroup.stack.EmiGroupStack
import dev.emi.emi.api.stack.EmiStack

data class BakedEmiStackGrouper(
    internal val stackSource: List<EmiStack>,
    internal val stackGroups: List<EmiStackGroupV2>,
    internal val preGroupedStackList: List<EmiStack>,
    internal val stackToStackGroups: Map<EmiStack, List<EmiStackGroupV2>>
) {

    override fun toString() = "BakedEmiStackGrouper[${stackSource.size}](stackGroups=$stackGroups)"

    fun search(searchedStacks: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedStackGroups = mutableSetOf<EmiStackGroupV2>()
        val stackGroupToGroupStack = stackGroups.associateWith { EmiGroupStack(it) }

        for (stack in searchedStacks) {
            val stackGroups = stackToStackGroups[stack]
            if (stackGroups == null) {
                result += stack
            } else { // Is in groups
                for (group in stackGroups) {
                    val groupStack = requireNotNull(stackGroupToGroupStack[group])
                    groupStack.addAndWrapStack(stack)

                    if (group !in addedStackGroups) { // If the group hasn't been added to list yet
                        result += groupStack
                        addedStackGroups += group
                    }
                }
            }
        }

        return result
    }

}