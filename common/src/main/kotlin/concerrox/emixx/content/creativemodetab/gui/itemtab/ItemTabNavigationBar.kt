package concerrox.emixx.content.creativemodetab.gui.itemtab

import com.google.common.collect.ImmutableList
import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.res
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.TabButton
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.components.tabs.TabNavigationBar
import net.minecraft.client.gui.layouts.LinearLayout

class ItemTabNavigationBar(private val tabManager: ItemTabManager) : TabNavigationBar(0, tabManager, arrayListOf()) {

    companion object {
        private val TEXTURE = res("textures/gui/buttons.png")
    }

    var x = 0
    var y = 0
    val height
        get() = layout.height

    fun pos(x: Int, y: Int): ItemTabNavigationBar {
        this.x = x
        this.y = y
        return this
    }

    fun setTabs(tabs: MutableList<ItemTab>) {
        this.tabs = ImmutableList.copyOf(tabs)
        // Replace the original layout so that we can customize the tab buttons
        layout = LinearLayout.horizontal()
        tabButtons = ImmutableList.copyOf(tabs.map {
            layout.addChild(ItemTabButton(tabManager, it, 0, ENTRY_SIZE))
        })
    }

    override fun render(raw: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (EmiScreenManager.isDisabled()) return

        RenderSystem.enableBlend()
        EmiDrawContext.wrap(raw).apply {
            drawTexture(TEXTURE, x, y + 2, 32, 0, 1, 16)
            drawTexture(TEXTURE, x + 1, y + 2, 32, 0, 1, 16)
            drawTexture(TEXTURE, x + width - 1, y + 2, 32, 0, 1, 16)
            drawTexture(TEXTURE, x + width - 2, y + 2, 32, 0, 1, 16)
        }
        tabButtons.forEach { it.render(raw, mouseX, mouseY, partialTick) }
        RenderSystem.disableBlend()
    }

    override fun arrangeElements() {
        tabButtons.forEach { it.width = ENTRY_SIZE }
        layout.arrangeElements()
        layout.x = x + 2
        layout.y = y
    }

    override fun setFocused(focusedChildren: GuiEventListener?) {
        if (super.getFocused() != null) {
            super.getFocused()!!.isFocused = false
        }

        if (focusedChildren != null) {
            focusedChildren.isFocused = true
        }

        // TODO: fix the recursion
//        focused = focusedChildren

        if (focusedChildren is TabButton) {
            tabManager.setCurrentTab(focusedChildren.tab(), false)
        }
    }

}