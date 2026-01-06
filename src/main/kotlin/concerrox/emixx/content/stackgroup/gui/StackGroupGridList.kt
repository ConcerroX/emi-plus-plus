package concerrox.emixx.content.stackgroup.gui

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.content.stackgroup.EmiGroupStack
import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.gui.components.Switch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class StackGroupGridList(
    private val screen: StackGroupConfigScreen, private val disabledStackGroups: MutableSet<ResourceLocation>
) : ContainerObjectSelectionList<StackGroupGridList.TripleEntry>(
    Minecraft.getInstance(), screen.width, screen.height, 0, TripleEntry.HEIGHT
) {

    init {
        centerListVertically = false
        setRenderHeader(true, 16)
    }

    override fun getRowWidth() = TripleEntry.WIDTH

    fun add() {
        StackGroupManager.stackGroupToGroupStacks.values.chunked(3).forEach { triple ->
            addEntry(TripleEntry(this, triple))
        }
    }

    class StackGroupEntry(private val triple: TripleEntry, private val stack: EmiGroupStack?) :
        AbstractContainerWidget(
            0, 0, WIDTH, HEIGHT, Component.empty()
        ) {

        companion object {
            private val BACKGROUND =
                ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png")
            private const val PADDING = 8
            private const val BORDER_WIDTH = 1
            const val WIDTH = ENTRY_SIZE * 8 + PADDING * 2 + BORDER_WIDTH * 2
            const val HEIGHT = ENTRY_SIZE * 2 + PADDING * 2 + BORDER_WIDTH * 2
        }

        private val switch = Switch.Builder(Component.empty())
            .setChecked(stack != null && !triple.listWidget.disabledStackGroups.contains(stack.group.id)).apply {
                onCheckedChangeListener = Switch.OnCheckedChangeListener { _, isChecked ->
                    if (stack != null) {
                        if (isChecked) {
                            triple.listWidget.disabledStackGroups.remove(stack.group.id)
                        } else {
                            triple.listWidget.disabledStackGroups.add(stack.group.id)
                        }
                    }
                }
            }.build()
        private val children = mutableListOf(switch)

        override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            renderBackground(guiGraphics)
            renderBorders(guiGraphics)
            val startX = x + BORDER_WIDTH + PADDING
            val startY = y + BORDER_WIDTH + PADDING
            stack?.let {
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    stack.group.id.toString(),
                    startX,
                    startY + 2,
                    0xFFFFFF
                )
                var itemX = startX
                val itemY = startY + ENTRY_SIZE
                stack.items.take(8).forEach {
                    it.render(guiGraphics, itemX, itemY, partialTick)
                    itemX += ENTRY_SIZE
                }
            }
            if (stack != null) {
                switch.setPosition(x + WIDTH - switch.width - BORDER_WIDTH - PADDING, startY)
                switch.render(guiGraphics, mouseX, mouseY, partialTick)
            }
        }

        override fun setFocused(isFocused: Boolean) {
            if (!isFocused) children.forEach {
                it.isFocused = false
            }
        }

        private fun renderBorders(guiGraphics: GuiGraphics) {
            RenderSystem.enableBlend()
            val resourceLocation = Screen.INWORLD_HEADER_SEPARATOR
            val resourceLocation2 = Screen.INWORLD_FOOTER_SEPARATOR
            guiGraphics.blit(resourceLocation, x, y, 0f, 0f, getWidth(), 2, 32, 2)
            guiGraphics.blit(resourceLocation2, x, bottom, 0f, 0f, getWidth(), 2, 32, 2)
            RenderSystem.disableBlend()
        }

        private fun renderBackground(guiGraphics: GuiGraphics) {
            RenderSystem.enableBlend()
            guiGraphics.blit(BACKGROUND, x, y, right.toFloat(), bottom.toFloat(), getWidth(), getHeight(), 32, 32)
            RenderSystem.disableBlend()
        }

        override fun isFocused() = triple.focused === this
        override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
        override fun children() = children

    }

    class TripleEntry(val listWidget: StackGroupGridList, stack: List<EmiGroupStack?>) : Entry<TripleEntry>() {

        companion object {
            const val GUTTER = 6
            const val WIDTH = StackGroupEntry.WIDTH * 3 + GUTTER * 2
            const val HEIGHT = StackGroupEntry.HEIGHT + GUTTER * 2
        }

        private val children = mutableListOf<StackGroupEntry>().apply {
            for (i in 0..2) {
                val stack = stack.getOrNull(i)?.let {
                    add(StackGroupEntry(this@TripleEntry, it))
                }
            }
        }

        override fun render(
            guiGraphics: GuiGraphics,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int,
            mouseX: Int,
            mouseY: Int,
            isHovered: Boolean,
            partialTick: Float
        ) {
            var xOffset = 0
            val startX = (listWidget.screen.width - WIDTH) / 2
            for (abstractWidget in children) {
                abstractWidget.setPosition(startX + xOffset, top)
                abstractWidget.render(guiGraphics, mouseX, mouseY, partialTick)
                xOffset += StackGroupEntry.WIDTH + GUTTER * 2
            }
        }

        override fun children() = children
        override fun narratables() = children
    }

}
