package concerrox.emixx.content.stackgroup.ui

import concerrox.emixx.Minecraft
import concerrox.emixx.content.ScreenManager
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.gui.components.Switch
import concerrox.emixx.oreui.OreUi
import concerrox.emixx.id
import concerrox.emixx.util.fastBlitNineSlicedSprite
import concerrox.emixx.util.renderScrollingString
import concerrox.emixx.util.with
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.CommonComponents

class StackGroupConfigGridViewV2() : ContainerObjectSelectionList<StackGroupConfigGridViewV2.RowEntry>(
    Minecraft, 0, 0, 0, ITEM_VIEW_HEIGHT + OreUi.SPACING_MEDIUM
) {

    companion object {
        private val SCROLLBAR_BACKGROUND_SPRITE = id("scrollbar/background")
        private val SCROLLBAR_SPRITE = id("button/secondary")
        private val ITEM_CARD_TOP_SPRITE = id("others/card_top")

        private const val ITEM_PREVIEW_COLUMN_COUNT = 8
        private const val ITEM_PREVIEW_ROW_COUNT = 2

        internal const val SCROLLBAR_WIDTH = 10
        internal const val ITEM_VIEW_WIDTH =
            OreUi.SPACING_MEDIUM + ITEM_PREVIEW_COLUMN_COUNT * ScreenManager.ENTRY_SIZE + OreUi.SPACING_MEDIUM
        private const val ITEM_VIEW_PREVIEW_HEIGHT =
            OreUi.SPACING_MEDIUM + ITEM_PREVIEW_ROW_COUNT * ScreenManager.ENTRY_SIZE + OreUi.SPACING_MEDIUM - 1
        private const val ITEM_VIEW_HEIGHT = ITEM_VIEW_PREVIEW_HEIGHT + 32
    }

    var columnCount = 4
        set(value) {
            field = value
            updateEntries()
        }

    var data = emptyList<EmiStackGroupV2>()
        set(value) {
            field = value
            updateEntries()
        }

    init {
        centerListVertically = false
        headerHeight = -4 // Remove the space between the header and the list
    }

    override fun getRowWidth() = width - 10
    override fun getRowLeft() = x
    override fun getScrollbarPosition() = rowRight + 4

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        enableScissor(guiGraphics)
        renderListItems(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.disableScissor()
        renderScrollbar(guiGraphics)
    }

    private fun renderScrollbar(guiGraphics: GuiGraphics) {
        if (!scrollbarVisible()) return
        val h = (height * height / maxPosition.toFloat()).toInt().coerceIn(32, height - 8)
        val top = (scrollAmount * (height - h - 4) / maxScroll + y).toInt().coerceAtLeast(y)
        guiGraphics.fastBlitNineSlicedSprite(SCROLLBAR_BACKGROUND_SPRITE, scrollbarPosition, y, 6, height - 4)
        guiGraphics.fastBlitNineSlicedSprite(SCROLLBAR_SPRITE, scrollbarPosition, top, 6, h)
    }

    private fun updateEntries() {
        clearEntries()
        data.chunked(columnCount).forEach {
            addEntry(RowEntry(it))
        }
    }

    class ItemView(private val rowEntry: RowEntry, private val stackGroup: EmiStackGroupV2) :
        AbstractContainerWidget(0, 0, ITEM_VIEW_WIDTH, ITEM_VIEW_HEIGHT, CommonComponents.EMPTY) {

        private val switch = Switch.Builder(CommonComponents.EMPTY).setChecked(stackGroup.isEnabled).build()
        private val children = listOf(switch)

        override fun children() = children
        override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
        override fun isFocused() = rowEntry.focused === this

        override fun setFocused(isFocused: Boolean) {
            if (!isFocused) children.forEach { it.isFocused = false }
        }

        override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            guiGraphics.fastBlitNineSlicedSprite(ITEM_CARD_TOP_SPRITE, x, y, ITEM_VIEW_WIDTH, ITEM_VIEW_PREVIEW_HEIGHT)
            renderItems(guiGraphics)

            val cardTop = y + ITEM_VIEW_PREVIEW_HEIGHT
            val cardActionLeft = x + ITEM_VIEW_WIDTH - 40
            guiGraphics.fastBlitNineSlicedSprite(OreUi.CARD_START_SPRITE, x, cardTop, ITEM_VIEW_WIDTH - 40, 32)
            guiGraphics.fastBlitNineSlicedSprite(OreUi.CARD_END_SPRITE, cardActionLeft, cardTop, 40, 32)

            guiGraphics.renderScrollingString(
                Minecraft.font,
                stackGroup.name,
                x + OreUi.SPACING_MEDIUM,
                cardTop + 7,
                cardActionLeft - OreUi.SPACING_MEDIUM,
                cardTop + 7 + 10,
                0xFFFFFF,
                false
            )
            guiGraphics.with {
                translate(x + OreUi.SPACING_MEDIUM.toFloat(), cardTop + 18f, 0f)
                scale(0.75f, 0.75f, 0.75f)
                guiGraphics.drawString(Minecraft.font, stackGroup.id.toString(), 0, 0, 0xD0D1D4, false)
            }

            switch.setPosition(cardActionLeft + 6, cardTop + OreUi.SPACING_MEDIUM)
            switch.render(guiGraphics, mouseX, mouseY, partialTick)
        }

        private fun renderItems(guiGraphics: GuiGraphics) {
            var itemX = x + OreUi.SPACING_MEDIUM + 1
            var itemY = y + OreUi.SPACING_MEDIUM + 1
            var idx = 0
            for (i in 0..<ITEM_PREVIEW_ROW_COUNT) {
                for (j in 0..<ITEM_PREVIEW_COLUMN_COUNT) {
                    val stack = stackGroup.collectedStacks.getOrNull(idx)?.itemStack
                    if (stack != null) guiGraphics.renderItem(stack, itemX, itemY)
                    itemX += 18
                    idx++
                }
                itemX = x + OreUi.SPACING_MEDIUM + 1
                itemY += 18
            }
        }

    }

    class RowEntry(rowItems: List<EmiStackGroupV2>) : Entry<RowEntry>() {

        private val children = rowItems.map { ItemView(this, it) }

        override fun children() = children
        override fun narratables() = children

        override fun render(
            guiGraphics: GuiGraphics,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int, // DO NOT USE THIS: it has an intrinsic padding of 4
            mouseX: Int,
            mouseY: Int,
            hovering: Boolean,
            partialTick: Float
        ) {
            var x = left
            for (itemView in children) {
                itemView.setPosition(x, top)
                itemView.render(guiGraphics, mouseX, mouseY, partialTick)
                x += itemView.width + OreUi.SPACING_MEDIUM
            }
        }

    }

}