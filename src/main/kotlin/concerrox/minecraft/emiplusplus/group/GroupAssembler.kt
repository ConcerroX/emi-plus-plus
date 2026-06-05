package concerrox.minecraft.emiplusplus.group

import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList

/**
 * Baked group assembler that maps flat stacks to group members.
 *
 * Created by [StackGroups.bake] after configs are loaded.
 * Used to transform the flat [EmiStackList.stacks] into a grouped list,
 * and to intercept search results to inject group headers.
 */
class GroupAssembler(
    val groups: List<GroupConfig>,
    val selectors: Map<String, List<GroupSelector>>,  // groupId -> selectors
) {

    /** Map from EmiStack to the group IDs it belongs to. */
    private val stackToGroupIds: Map<EmiStack, List<String>> = buildStackToGroupMap()

    /** Pre-built EmiGroupStack instances keyed by group ID. */
    private val groupStacks: Map<String, EmiGroupStack> = buildGroupStacks()

    private fun buildStackToGroupMap(): Map<EmiStack, List<String>> {
        val map = mutableMapOf<EmiStack, MutableList<String>>()
        for (group in groups) {
            val groupSelectors = selectors[group.id] ?: continue
            for (stack in EmiStackList.stacks) {
                if (groupSelectors.any { it.match(stack) }) {
                    map.getOrPut(stack) { mutableListOf() }.add(group.id)
                }
            }
        }
        return map
    }

    private fun buildGroupStacks(): Map<String, EmiGroupStack> {
        return groups.associate { group ->
            group.id to EmiGroupStack(group.id, group.name)
        }
    }

    /**
     * Transform search results: insert [EmiGroupStack] headers for any groups
     * that have members in the search results. Ungrouped stacks pass through.
     */
    fun search(searchedStacks: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedGroups = mutableSetOf<String>()
        val groupStacksCopy = groupStacks.mapValues { (_, gs) ->
            EmiGroupStack(gs.groupId, gs.groupName).also { it.isExpanded = gs.isExpanded }
        }

        for (stack in searchedStacks) {
            val groupIds = stackToGroupIds[stack]
            if (groupIds == null) {
                // Not in any group — pass through
                result += stack
            } else {
                for (groupId in groupIds) {
                    val groupStack = groupStacksCopy[groupId] ?: continue
                    groupStack.addMember(stack)

                    if (groupId !in addedGroups) {
                        result += groupStack
                        addedGroups += groupId
                    }
                }
            }
        }

        return result
    }

    /**
     * Build the full grouped index list from filteredStacks.
     */
    fun buildIndexStacks(): List<EmiStack> {
        return search(EmiStackList.stacks)
    }

    /**
     * Find which group IDs a given stack belongs to.
     */
    fun getGroupIdsFor(stack: EmiStack): List<String> = stackToGroupIds[stack] ?: emptyList()
}
