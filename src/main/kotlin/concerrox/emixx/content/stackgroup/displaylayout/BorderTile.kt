package concerrox.emixx.content.stackgroup.displaylayout

import net.minecraft.client.gui.GuiGraphics
import kotlin.experimental.and

class BorderTile(tileX: Int, tileY: Int, val borderParts: Short) : StackDisplayLayout.Tile(tileX, tileY) {

    companion object {
        private const val BORDER_COLOR = 0x66FFFFFF
    }

    override fun render(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val left = x + tileX * 18
        val top = y + tileY * 18

        if (has(BorderPart.LEFT)) {
            guiGraphics.fill(left, top, left + 1, top + 18, BORDER_COLOR)
        }
        if (has(BorderPart.TOP)) {
            guiGraphics.fill(left, top, left + 18, top + 1, BORDER_COLOR)
        }
        if (has(BorderPart.RIGHT)) {
            guiGraphics.fill(left + 17, top, left + 18, top + 18, BORDER_COLOR)
        }
        if (has(BorderPart.BOTTOM)) {
            guiGraphics.fill(left, top + 17, left + 18, top + 18, BORDER_COLOR)
        }

        // Inner corners
        if (has(BorderPart.TOP_LEFT)) {
            guiGraphics.fill(left, top, left + 1, top + 1, BORDER_COLOR)
        }
        if (has(BorderPart.TOP_RIGHT)) {
            guiGraphics.fill(left + 17, top, left + 18, top + 1, BORDER_COLOR)
        }
        if (has(BorderPart.BOTTOM_LEFT)) {
            guiGraphics.fill(left, top + 17, left + 1, top + 18, BORDER_COLOR)
        }
        if (has(BorderPart.BOTTOM_RIGHT)) {
            guiGraphics.fill(left + 17, top + 17, left + 18, top + 18, BORDER_COLOR)
        }

    }

    fun has(target: BorderPart) = borderParts and target.bit == target.bit

    enum class BorderPart(val bit: Short) {
        LEFT(1), TOP(2), RIGHT(4), BOTTOM(8), TOP_LEFT(16), TOP_RIGHT(32), BOTTOM_LEFT(64), BOTTOM_RIGHT(128);
    }

}