package concerrox.minecraft.emiplusplus.group

import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import java.util.IdentityHashMap

/**
 * Baked group assembler that maps flat stacks to group members.
 * Uses IdentityHashMap for initial index build (reference-based, avoids EmiStack's loose equals).
 * Uses selector-based matching in search() for compatibility with search result stacks.
 */
class GroupAssembler(
    val groups: List<GroupConfig>,
    val selectors: Map<String, List<GroupSelector>>,
) {

    /** IdentityHashMap: matches EmiStackList.stacks references used in buildIndexStacks() */
    private val stackToGroupIds: Map<EmiStack, List<String>> = buildStackToGroupMap()

    private val groupStacks: Map<String, EmiGroupStack> = buildGroupStacks()

    private fun buildStackToGroupMap(): Map<EmiStack, List<String>> {
        val map = IdentityHashMap<EmiStack, MutableList<String>>()
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
            group.id to EmiGroupStack(group.id, group.name, group.borderColor)
        }
    }

    /**
     * Transform a stack list by injecting group headers. Works for both index and search results.
     * Uses selector-based matching so it works with any EmiStack instances (not just original references).
     */
    fun search(searchedStacks: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedGroups = mutableSetOf<String>()
        val groupStacksCopy = groupStacks.mapValues { (_, gs) ->
            EmiGroupStack(gs.groupId, gs.groupName, gs.borderColor).also { it.isExpanded = gs.isExpanded }
        }

        for (stack in searchedStacks) {
            val matchingGroupIds = groups
                .filter { group -> (selectors[group.id] ?: emptyList()).any { it.match(stack) } }
                .map { it.id }

            if (matchingGroupIds.isEmpty()) {
                result += stack
            } else {
                for (groupId in matchingGroupIds) {
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

    fun buildIndexStacks(): List<EmiStack> {
        // Use IdentityHashMap lookup for index build (same objects as EmiStackList.stacks)
        val result = mutableListOf<EmiStack>()
        val addedGroups = mutableSetOf<String>()
        val groupStacksCopy = groupStacks.mapValues { (_, gs) ->
            EmiGroupStack(gs.groupId, gs.groupName, gs.borderColor).also { it.isExpanded = gs.isExpanded }
        }

        for (stack in EmiStackList.stacks) {
            val groupIds = stackToGroupIds[stack]
            if (groupIds == null) {
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
}
