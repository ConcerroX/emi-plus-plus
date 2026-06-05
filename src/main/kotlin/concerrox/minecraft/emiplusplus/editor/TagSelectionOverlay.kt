package concerrox.minecraft.emiplusplus.editor

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

/**
 * Simple overlay that lists available tags for a clicked item.
 * User clicks a tag to select it, or clicks outside to cancel.
 */
class TagSelectionOverlay(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int,
    private val tags: List<Pair<String, String>>,  // notation to display name
    private val onSelect: (Pair<String, String>) -> Unit,
) {
    private val entryHeight = 18
    private val visibleCount = minOf(tags.size, 10)
    private var scroll: Int = 0

    fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        // Background
        graphics.fill(x, y, x + width, y + height, 0xDD000000.toInt())
        graphics.renderOutline(x, y, width, height, 0xFFFFFFFF.toInt())

        // Title
        graphics.drawCenteredString(
            Minecraft.getInstance().font,
            "Select a tag",
            x + width / 2, y + 4, 0xFFFFFF
        )

        // Tag list
        for (i in 0 until visibleCount) {
            val idx = scroll + i
            if (idx >= tags.size) break

            val (notation, displayName) = tags[idx]
            val ey = y + 20 + i * entryHeight

            val isHovered = mouseX in x..(x + width) && mouseY in ey..(ey + entryHeight)
            if (isHovered) {
                graphics.fill(x + 2, ey, width - 4, entryHeight, 0x44FFFFFF.toInt())
            }

            graphics.drawString(
                Minecraft.getInstance().font,
                "#$displayName", x + 6, ey + 5, if (isHovered) 0xFFFF55 else 0xCCCCCC
            )
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return false
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            onSelect("" to "") // clicking outside cancels (empty pair signals cancel)
            return true
        }

        for (i in 0 until visibleCount) {
            val idx = scroll + i
            if (idx >= tags.size) break
            val ey = y + 20 + i * entryHeight
            if (mouseY >= ey && mouseY <= ey + entryHeight) {
                onSelect(tags[idx])
                return true
            }
        }
        return true
    }
}
