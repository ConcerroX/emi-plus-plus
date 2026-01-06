package concerrox.emixx.oreui.component

import concerrox.emixx.oreui.OreUi
import concerrox.emixx.id
import concerrox.emixx.util.fastBlitNineSlicedSprite
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.CommonComponents

class ScrollView(var itemFillMaxWidth: Boolean = true) : AbstractContainerWidget(0, 0, 0, 0, CommonComponents.EMPTY) {

    companion object {
        private const val SCROLL_MULTIPLIER = 8
        private const val SCROLLBAR_WIDTH = 10

        private val SCROLLBAR_BACKGROUND_SPRITE = id("scrollbar/background")
        private val SCROLLBAR_SPRITE = id("button/secondary")
        private val CONTAINER_VIEW_BACKGROUND = id("navigation_view/background")
    }

    var marginTop = OreUi.SPACING_SMALL
    var marginBottom = OreUi.SPACING_SMALL

    private val children = mutableListOf<AbstractWidget>()
    private val accumulatedItemHeights = mutableListOf<Int>()
    private var scrollAmount = 0
        set(value) {
            field = value.coerceIn(0, maxPosition - height)
        }
    private val maxPosition: Int get() = accumulatedItemHeights.last()

    private val internalTop: Int get() = y + marginTop
    private val internalHeight: Int get() = height - marginTop - marginBottom

    fun setItems(vararg widgets: AbstractWidget) {
        children.clear()
        accumulatedItemHeights.clear()
        add(Spacer(0, 0, 0, marginTop + 1))
        widgets.forEach { add(it) }
        add(Spacer(0, 0, 0, marginBottom + 1))
    }

    fun add(widget: AbstractWidget) {
        children += widget
        accumulatedItemHeights += (accumulatedItemHeights.lastOrNull() ?: 0) + widget.height
    }

    override fun children() = children
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

    override fun renderWidget(
        guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float
    ) {
        val backgroundTop = ((if (isRowVisible(1)) getRowTop(1) else y) - 1).coerceAtLeast(y)
        guiGraphics.fastBlitNineSlicedSprite(
            CONTAINER_VIEW_BACKGROUND,
            x,
            backgroundTop,
            getRowMaxWidth() + 2,
            (if (isRowVisible(children.lastIndex - 1)) getRowBottom(children.lastIndex - 1) - backgroundTop else height) + 1
        )
        guiGraphics.enableScissor(x, y, right, bottom)
        renderListItems(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.disableScissor()
        renderScrollbar(guiGraphics)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scrollAmount -= scrollY.toInt() * SCROLL_MULTIPLIER
        return true
    }

    private fun renderListItems(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        for (i in 0..<children.size) {
            val itemTop = getRowTop(i)
            val itemBottom = getRowBottom(i)
            if (itemBottom >= y && itemTop <= bottom) {
                renderItem(children[i], guiGraphics, mouseX, mouseY, partialTick, x, itemTop)
            }
        }
    }

    private fun isRowVisible(index: Int): Boolean {
        val itemTop = getRowTop(index)
        val itemBottom = getRowBottom(index)
        return itemBottom >= y && itemTop <= bottom
    }

    private fun getRowTop(index: Int) = y - scrollAmount + accumulatedItemHeights.getOrElse(index - 1) { 0 }
    private fun getRowBottom(index: Int) = y - scrollAmount + accumulatedItemHeights[index]
    private fun getRowMaxWidth() = width - SCROLLBAR_WIDTH - 2
    private fun getRowRight() = x + 1 + getRowMaxWidth()

    private fun renderItem(
        item: AbstractWidget,
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
        left: Int,
        top: Int,
    ) {
        item.x = left + 1
        item.y = top
        if (itemFillMaxWidth) item.width = getRowMaxWidth()
        item.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    private fun renderScrollbar(guiGraphics: GuiGraphics) {
        val scrollBarLeft = getRowRight() + 4
        val scrollBarTop = internalTop + height * scrollAmount / maxPosition
        val scrollBarHeight = internalHeight * height / maxPosition
        guiGraphics.fastBlitNineSlicedSprite(SCROLLBAR_BACKGROUND_SPRITE, scrollBarLeft, internalTop, 6, internalHeight)
        guiGraphics.fastBlitNineSlicedSprite(SCROLLBAR_SPRITE, scrollBarLeft, scrollBarTop, 6, scrollBarHeight)
    }

}