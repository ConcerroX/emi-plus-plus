package concerrox.emixx.content

import concerrox.emixx.config.EmiPlusPlusKeyMappings
import concerrox.emixx.content.stackgroup.StackGroups
import concerrox.emixx.content.stackgroup.displaylayout.StackDisplayLayout
import concerrox.emixx.content.stackgroup.stack.EmiGroupStack
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack

object StackManager {

    @JvmStatic
    val layout = StackDisplayLayout()

    @JvmStatic
    var indexStacks = StackGroups.indexStackGrouper.preGroupedStackList
        private set

    @JvmStatic
    @Suppress("unchecked_cast")
    fun afterSearchedStacks(stacks: List<EmiIngredient>) {
        indexStacks = StackGroups.indexStackGrouper.search(stacks as List<EmiStack>)
    }

    @JvmStatic
    fun onClickStack(stack: EmiIngredient, button: Int) = when (stack) {
        is EmiGroupStack -> onClickGroupStack(stack)
        is GroupedEmiStackWrapper<*> -> onClickGroupedStackWrapper(stack, button)
        else -> false
    }

    private fun onClickGroupStack(groupStack: EmiGroupStack): Boolean {
        if (groupStack.isExpanded) collapseStackGroup(groupStack) else expandStackGroup(groupStack)
        return true
    }

    private fun onClickGroupedStackWrapper(wrapper: GroupedEmiStackWrapper<*>, button: Int): Boolean {
        if (!EmiPlusPlusKeyMappings.collapseGroup.matchesMouse(button)) return false
        collapseStackGroup(wrapper.groupStack)
        return true
    }

    private fun expandStackGroup(groupStack: EmiGroupStack) {
        val groupIdx = indexStacks.indexOf(groupStack)
        val groupContent = groupStack.itemsNew
        val newIndexStacks = indexStacks.toMutableList()
        newIndexStacks.addAll(groupIdx + 1, groupContent)
        indexStacks = newIndexStacks
        groupStack.isExpanded = true
    }

    private fun collapseStackGroup(groupStack: EmiGroupStack) {
        val groupIdx = indexStacks.indexOf(groupStack)
        val groupContent = groupStack.itemsNew
        val newIndexStacks = indexStacks.toMutableList()
        repeat(groupContent.size) { newIndexStacks.removeAt(groupIdx + 1) }
        indexStacks = newIndexStacks
        groupStack.isExpanded = false
    }

}