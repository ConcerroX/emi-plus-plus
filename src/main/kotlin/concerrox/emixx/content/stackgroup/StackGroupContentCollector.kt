package concerrox.emixx.content.stackgroup

import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import dev.emi.emi.api.stack.EmiStack

@Deprecated("")
object StackGroupContentCollector {

    fun collect(source: List<EmiStack>, stackGroups: List<EmiStackGroupV2>): List<EmiStackGroupV2> {
        return stackGroups
    }

}