package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

abstract class StackGroup(val id: ResourceLocation, val name: Component, val isEnabled: Boolean) {

    val configFilename get() = id.toString().replace(":", "__").replace("/", "__") + ".json"

    abstract fun match(stack: EmiStack): Boolean

    // TODO: use field getter
    fun getName(): Component = Component.translatable(null)

}