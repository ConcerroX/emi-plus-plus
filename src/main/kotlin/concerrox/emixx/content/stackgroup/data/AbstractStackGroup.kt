package concerrox.emixx.content.stackgroup.data

import concerrox.emixx.Identifier
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.network.chat.Component

abstract class AbstractStackGroup(val id: Identifier, val name: String, val isEnabled: Boolean) {

    companion object {
        fun buildConfigFilename(id: String): String {
            return id.replace(":", "__").replace("/", "__") + ".json"
        }
    }

    val configFilename get() = buildConfigFilename(id.toString())

    abstract fun match(stack: EmiStack): Boolean

    /**
     * This method is blocking and should be called asynchronously.
     */
    abstract fun loadContent(): List<EmiStack>

    // TODO: use field getter
    fun getNameComp(): Component = Component.translatable(null)

}