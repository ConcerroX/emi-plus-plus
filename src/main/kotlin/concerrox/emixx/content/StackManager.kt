package concerrox.emixx.content

import concerrox.emixx.content.stackgroup.StackGroups
import concerrox.emixx.content.stackgroup.displaylayout.StackDisplayLayout
import concerrox.emixx.content.stackgroup.stack.EmiGroupStack
import concerrox.emixx.util.logDebug
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.screen.EmiScreenManager

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
    fun onClickStack(stack: EmiIngredient) {
        logDebug("onClickStack: ${stack.javaClass.simpleName} $stack")
        val groupStack = stack as? EmiGroupStack ?: return

        val groupIdx = indexStacks.indexOf(groupStack)
        val groupContent = groupStack.itemsNew
        val newIndexStacks = indexStacks.toMutableList()

        if (groupStack.isExpanded) {
            repeat(groupContent.size) { newIndexStacks.removeAt(groupIdx + 1) }
        } else {
            newIndexStacks.addAll(groupIdx + 1, groupContent)
        }

        groupStack.isExpanded = !groupStack.isExpanded
        indexStacks = newIndexStacks
    }

    private fun recalculate() {
        EmiScreenManager.recalculate()
    }

}