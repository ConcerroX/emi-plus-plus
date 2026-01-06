package concerrox.emixx

import com.google.gson.JsonElement
import concerrox.emixx.content.stackgroup.GroupedEmiStack
import concerrox.emixx.content.villagertrade.VillagerTradeManager
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiInitRegistry
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import dev.emi.emi.registry.EmiIngredientSerializers

@EmiEntrypoint
class EmiPlusPlusPlugin : EmiPlugin {

    override fun register(registry: EmiRegistry) {
        VillagerTradeManager.initialize(registry)
    }

    override fun initialize(registry: EmiInitRegistry) {
        registry.addIngredientSerializer(
            GroupedEmiStack::class.java, object : EmiIngredientSerializer<GroupedEmiStack<*>> {
                override fun getType() = "grouped"
                override fun deserialize(element: JsonElement) = throw IllegalStateException("")
                @Suppress("UNCHECKED_CAST")
                override fun serialize(stack: GroupedEmiStack<*>): JsonElement? {
                    val serializer =
                        EmiIngredientSerializers.BY_CLASS[stack.realStack.javaClass] as? EmiIngredientSerializer<EmiStack>
                    return serializer?.serialize(stack.realStack)
                }
            })
    }

}