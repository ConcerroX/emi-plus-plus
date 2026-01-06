package concerrox.emixx.content.stackgroup

import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiIngredientSerializers
import dev.emi.emi.registry.EmiTags
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

object StackGroupContentCollector {

    fun collect(source: List<EmiStack>, stackGroups: List<EmiStackGroupV2>): List<EmiStackGroupV2> {
        for (group in stackGroups) collect(source, group)
        return stackGroups
    }

    private fun collect(source: List<EmiStack>, stackGroup: EmiStackGroupV2) {
        stackGroup.collectedStacks.clear()
        stackGroup.contentGroupingRules.forEach {
            when (it) {
                is EmiStackGroupV2.ContentGroupingRule.Tag -> {
                    stackGroup.collectedStacks += EmiTags.getRawValues(
                        TagKey.create<Any>(ResourceKey.createRegistryKey(ResourceLocation.parse(it.type)), it.tag)
                    )
                }

                is EmiStackGroupV2.ContentGroupingRule.Stack -> {
                    stackGroup.collectedStacks += EmiIngredientSerializers.deserialize(it.stack) as? EmiStack
                        ?: throw IllegalArgumentException("Invalid stack: $it")
                }

                is EmiStackGroupV2.ContentGroupingRule.Regex -> {
                    stackGroup.collectedStacks += source.filter { stack ->
                        it.expression.matches(stack.id.toString())
                    }
                }
            }
        }
    }

}