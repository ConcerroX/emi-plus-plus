package concerrox.emixx.stack

import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class BlankStack : EmiStack() {

    override fun render(draw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) {}

    override fun isEmpty(): Boolean {
        return true
    }

    override fun copy(): EmiStack {
        TODO("Not yet implemented")
    }

    override fun getComponentChanges(): DataComponentPatch {
        TODO("Not yet implemented")
    }

    override fun getKey(): Any {
        TODO("Not yet implemented")
    }

    override fun getId(): ResourceLocation {
        TODO("Not yet implemented")
    }

    override fun getTooltipText(): MutableList<Component> {
        TODO("Not yet implemented")
    }

    override fun getName(): Component {
        TODO("Not yet implemented")
    }

}