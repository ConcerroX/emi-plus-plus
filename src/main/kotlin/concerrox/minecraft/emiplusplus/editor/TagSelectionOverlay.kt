package concerrox.minecraft.emiplusplus.editor

import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class TagSelectionOverlay(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int,
    private val tags: List<Pair<String, String>>,
    private val onSelect: (Pair<String, String>) -> Unit,
) {
    private val TEXTURE = EmiPort.id("emi", "textures/gui/background.png")
    private val entryHeight = 18

    fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val emiContext = EmiDrawContext.wrap(graphics)

        // Card background (9-patch, like editor cards)
        EmiRenderHelper.drawNinePatch(emiContext, TEXTURE, x, y, width, height, 27, 0, 4, 1)

        val visibleCount = minOf(tags.size, (height - 4) / entryHeight)
        for (i in 0 until visibleCount) {
            val idx = i
            if (idx >= tags.size) break
            val (notation, displayName) = tags[idx]
            val ey = y + 4 + i * entryHeight
            val hovered = mouseX in x..(x + width) && mouseY in ey..(ey + entryHeight)

            if (hovered) {
                emiContext.fill(x + 4, ey, width - 8, entryHeight, 0x44FFFFFF.toInt())
            }

            graphics.drawString(
                Minecraft.getInstance().font,
                "#$displayName",
                x + 6, ey + 4,
                if (hovered) 0xFFFFFF else 0x888888,
                hovered
            )
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return false
        val visibleCount = minOf(tags.size, (height - 4) / entryHeight)
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            onSelect("" to "")
            return true
        }
        for (i in 0 until visibleCount) {
            if (i >= tags.size) break
            val ey = y + 4 + i * entryHeight
            if (mouseY >= ey && mouseY <= ey + entryHeight) {
                onSelect(tags[i])
                return true
            }
        }
        return true
    }
}
