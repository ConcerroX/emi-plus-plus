package concerrox.emixx.oreui.component

import concerrox.emixx.Minecraft
import concerrox.emixx.oreui.OreUi
import concerrox.emixx.id
import concerrox.emixx.util.fastBlitNineSlicedSprite
import concerrox.emixx.util.renderScrollingString
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class TextButton(
    message: Component,
    x: Int = 0,
    y: Int = 0,
    width: Int = 150,
    height: Int = SIZE_NORMAL,
    var buttonStyle: Style = Style.SECONDARY,
    onClickedListener: OnClickedListener,
) : Button(
    if (buttonStyle == Style.HERO) OreUi.createTitleComponent(message) else message,
    x,
    y,
    width,
    height,
    onClickedListener = onClickedListener
) {

    companion object {
        const val SIZE_NORMAL = 24
        const val SIZE_LARGE = 28
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.fastBlitNineSlicedSprite(
            buttonStyle.getSprite(this),
            x,
            if (isPressedOrChecked) y + 2 else y,
            width,
            if (isPressedOrChecked) height - 2 else height,
        )
        renderString(guiGraphics, Minecraft.font, if (buttonStyle.isLight) 0x1E1E1F else 0xFFFFFF)

        // Checked indicator
        val indicatorX = x + width / 2 - 11
        val indicatorY = y + height - 2
        if (isChecked) guiGraphics.fill(indicatorX, indicatorY, indicatorX + 22, indicatorY + 1, 0xFFFFFFFF.toInt())
    }

    override fun renderScrollingString(guiGraphics: GuiGraphics, font: Font, width: Int, color: Int) {
        val i = x + width
        val j = x + this.getWidth() - width
        val yOffset = if (isPressedOrChecked) 2 else 0
        guiGraphics.renderScrollingString(
            font, message, i, y + yOffset, j, y + yOffset + height, color, buttonStyle == Style.HERO
        )
    }

    enum class Style(spriteName: String, val isLight: Boolean) {

        TAB("tab", false), HERO("primary", false), PRIMARY("primary", false), SECONDARY(
            "secondary", true
        ),
        REALMS("realms", false), DESTRUCTIVE("destructive", false);

        private val sprite = id("button/${spriteName}")
        private val hoveredSprite = id("button/${spriteName}_hovered")
        private val pressedSprite = id("button/${spriteName}_pressed")
        private val disabledSprite = id("button/disabled")

        fun getSprite(button: TextButton) = if (!button.isActive) {
            disabledSprite
        } else if (button.isPressedOrChecked) {
            pressedSprite
        } else if (button.isHovered) {
            hoveredSprite
        } else {
            sprite
        }

    }

}