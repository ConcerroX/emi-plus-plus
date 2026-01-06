package concerrox.emixx.oreui.component

import concerrox.emixx.id
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class IconButton(
    private val iconSprite: ResourceLocation,
    message: Component,
    x: Int = 0,
    y: Int = 0,
    width: Int = 20,
    height: Int = 20,
    onClickedListener: OnClickedListener,
) : Button(
    message, x, y, width, height, onClickedListener = onClickedListener
) {

    companion object {
        private val HIGHLIGHT_HOVERED = id("textures/gui/sprites/icon_button/highlight_on_light_hovered.png")
        private val HIGHLIGHT_PRESSED = id("textures/gui/sprites/icon_button/highlight_on_light_pressed.png")
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val highlightSprite = if (isPressedOrChecked) HIGHLIGHT_PRESSED else if (isHovered) HIGHLIGHT_HOVERED else null
        highlightSprite?.let {
            guiGraphics.blit(it, x, y, 0f, 0f, width, height, width, height)
        }
        guiGraphics.blit(iconSprite, x, y, 0f, 0f, width, height, width, height)
    }

}