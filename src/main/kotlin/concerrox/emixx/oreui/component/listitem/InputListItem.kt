package concerrox.emixx.oreui.component.listitem

import concerrox.emixx.Minecraft
import concerrox.emixx.oreui.OreUi
import concerrox.emixx.id
import concerrox.emixx.util.fastBlitNineSlicedSprite
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class InputListItem(message: Component) : AbstractContainerWidget(0, 0, 0, 42, message) {

    companion object {
        private val BACKGROUND_SPRITE = id("list_item/background")
    }

    private val children = mutableListOf(EditText(message))

    override fun renderWidget(
        guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float
    ) {
        guiGraphics.fastBlitNineSlicedSprite(BACKGROUND_SPRITE, x, y, width, height)
        guiGraphics.drawString(
            Minecraft.font, message, x + OreUi.SPACING_MEDIUM, y + OreUi.SPACING_MEDIUM, 0xFFFFFF, false
        )
        children.forEach {
            it.setPosition(x + 8, y + 20)
            it.setSize(width - 16, 20)
            it.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun children() = children
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

}