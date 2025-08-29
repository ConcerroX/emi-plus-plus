package concerrox.emixx.content

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.content.stackgroup.EmiGroupStack
import concerrox.emixx.content.stackgroup.StackGroupManager
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.search.EmiSearch

typealias Array2D<T> = Array<Array<T>>

object StackManager {

    /**
     * Just the unprocessed index stacks from EMI.
     */
    internal var indexStacks = EmiStackList.filteredStacks

    /**
     * The stacks to be searched, could be from the index, a creative mode tab, or a custom collection tab.
     */
    internal var sourceStacks = listOf<EmiStack>()

    /**
     * The stacks that have been searched
     */
    internal var searchedStacks = listOf<EmiStack>()

    /**
     * Stacks that include stack groups and stack collections.
     */
    private var groupedStacks = listOf<EmiStack>()

    /**
     * Index stacks that include stack groups and stack collections, so we don't have to group them every time.
     */
    private var groupedIndexStacks = listOf<EmiStack>()

    /**
     * Stacks that are already laid out on the grid, and are going to be displayed.
     */
    internal var displayedStacks = mutableListOf<EmiStack>()

    /**
     * A layout for the stacks -------------------, recreated every time when first rendered
     */
    internal var stackGrid = arrayOf(arrayOf<EmiStack?>())

    /**
     * --------------------------------------
     */
    internal var stackTextureGrid = mutableListOf<Layout.Tile>()

    internal fun reload() {
        indexStacks = EmiStackList.filteredStacks
        groupedIndexStacks = listOf()
        StackGroupManager.buildGroupedEmiStacksAndStackGroupToContents(indexStacks)
        updateSourceStacks(indexStacks)
    }

    internal fun updateSourceStacks(sourceStacks: List<EmiStack>) {
        this.sourceStacks = sourceStacks
        buildStacks(sourceStacks)
    }

    internal fun search(sourceStacks: List<EmiStack>, keyword: String) {
        this.sourceStacks = sourceStacks
        EmiSearch.search(keyword)
    }

    internal fun buildStacks(searchedStacks: List<EmiStack>) {
        this.searchedStacks = searchedStacks
        buildGroupedStacks()
        buildDisplayedStacks()
    }

    private fun buildGroupedStacks() {
        // If we're using the index stacks, use the grouped index stacks so we don't have to group them every time
        groupedStacks = if (searchedStacks == indexStacks) groupedIndexStacks.map {
            // TODO: fix this
//            if (it is EmiGroupStack) it.isExpanded = false
            it
        }.ifEmpty {
            // Build the grouped index stacks if they haven't been built
            groupedIndexStacks = StackGroupManager.buildGroupedStacks(searchedStacks)
            groupedIndexStacks
        } else StackGroupManager.buildGroupedStacks(searchedStacks)
    }

    // TODO: refactor
    private fun buildDisplayedStacks() {
        displayedStacks = groupedStacks.toMutableList()
        var i = 0
        while (i < displayedStacks.size) {
            val emiStack = displayedStacks[i]
            if (emiStack is EmiGroupStack) {
                if (emiStack.items.size == 1) {
                    displayedStacks[i] = emiStack.items[0].realStack
                } else if (emiStack.isExpanded) {
                    displayedStacks.addAll(i + 1, emiStack.items)
                }
            }
            i++
        }
    }

    @Deprecated("")
    fun onStackInteractionDeprecated(ingredient: EmiIngredient) {
        Layout.isTextureDirty = true
        EmiPlusPlus.LOGGER.info("onStackInteraction: $ingredient")
        when (ingredient) {
            is EmiGroupStack -> {
                val stacks = displayedStacks
                if (ingredient.isExpanded) {
                    for (i in 0 until ingredient.items.size) {
                        stacks.removeAt(stacks.indexOf(ingredient) + 1)
                    }
                } else {
                    stacks.addAll(stacks.indexOf(ingredient) + 1, ingredient.items)
                }
                // TODO: fix this
                displayedStacks = stacks.toMutableList()
                EmiScreenManager.recalculate()
                ingredient.isExpanded = !ingredient.isExpanded
            }

            else -> {}
        }
    }

}