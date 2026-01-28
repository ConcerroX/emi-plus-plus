package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiStack

data class BakedEmiStackGrouper(
    internal val stackSource: List<EmiStack>,
    internal val stackGroups: List<EmiStackGroupV2>,
    internal val preGroupedStackList: List<EmiStack>,
    internal val stackToStackGroups: Map<EmiStack, List<EmiStackGroupV2>>
) {

    fun search(searchedStacks: List<EmiStack>) {

    }

    override fun toString(): String {
        return "BakedEmiStackGrouper[${stackSource.size}](stackGroups=$stackGroups)"
    }

}