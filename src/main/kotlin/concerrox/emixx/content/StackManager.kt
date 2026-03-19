package concerrox.emixx.content

import concerrox.emixx.config.EmiPlusPlusKeyMappings
import concerrox.emixx.content.stackgroup.StackGroups
import concerrox.emixx.content.stackgroup.data.AbstractStackGroup
import concerrox.emixx.content.stackgroup.displaylayout.StackDisplayLayout
import concerrox.emixx.content.stackgroup.stack.EmiGroupStack
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.input.EmiBind
import java.util.function.Function

object StackManager {

    @JvmStatic
    val layout = StackDisplayLayout()

    @JvmStatic
    var indexStacks = StackGroups.indexStackGrouper.preGroupedStackList
        private set

    @JvmStatic
    val expandedStackGroups = mutableSetOf<AbstractStackGroup>()

    @JvmStatic
    @Suppress("unchecked_cast")
    fun afterSearchedStacks(stacks: List<EmiIngredient>) {
        indexStacks = StackGroups.indexStackGrouper.search(stacks as List<EmiStack>)
        expandStackGroups(expandedStackGroups.toSet())
    }

    @JvmStatic
    fun onClickStack(stack: EmiIngredient, function: Function<EmiBind, Boolean>) = when (stack) {
        is EmiGroupStack -> onClickGroupStack(stack)
        is GroupedEmiStackWrapper<*> -> onClickGroupedStackWrapper(stack, function)
        else -> false
    }

    private fun onClickGroupStack(groupStack: EmiGroupStack): Boolean {
        if (groupStack.isExpanded) collapseStackGroup(groupStack.group) else expandStackGroups(setOf(groupStack.group))
        return true
    }

    private fun onClickGroupedStackWrapper(
        wrapper: GroupedEmiStackWrapper<*>, function: Function<EmiBind, Boolean>
    ): Boolean {
        if (!function.apply(EmiPlusPlusKeyMappings.collapseGroup)) return false
        collapseStackGroup(wrapper.groupStack.group)
        return true
    }

    private fun expandStackGroups(stackGroups: Set<AbstractStackGroup>) {
        val expandedContents = linkedMapOf<Int, List<EmiStack>>()
        indexStacks.forEachIndexed { index, stack ->
            if (stack is EmiGroupStack && stack.group in stackGroups && !stack.isExpanded) {
                expandedContents[index + 1] = stack.itemsNew
                stack.isExpanded = true
            }
        }

        // Ensure capacity for expanded stacks to avoid reallocations and copying many times
        val newIndexStacks = ArrayList(indexStacks)
        newIndexStacks.ensureCapacity(newIndexStacks.size + expandedContents.values.sumOf { it.size })

        // Reverse the map to insert the expanded stacks in the correct order
        expandedContents.reversed().forEach { (pos, items) ->
            newIndexStacks.addAll(pos, items)
        }

        expandedStackGroups += stackGroups
        indexStacks = newIndexStacks
    }

    private fun collapseStackGroup(stackGroup: AbstractStackGroup) {
        val stackIdx = indexStacks.indexOfFirst { it is EmiGroupStack && it.group == stackGroup }
        val stack = indexStacks[stackIdx] as EmiGroupStack

        val newIndexStacks = indexStacks.toMutableList()
        repeat(stack.itemsNew.size) { newIndexStacks.removeAt(stackIdx + 1) }
        indexStacks = newIndexStacks
        expandedStackGroups -= stackGroup
        stack.isExpanded = false
    }

}