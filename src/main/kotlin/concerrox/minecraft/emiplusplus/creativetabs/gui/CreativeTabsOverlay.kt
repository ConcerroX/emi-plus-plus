package concerrox.minecraft.emiplusplus.creativetabs.gui

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.minecraft.emiplusplus.creativetabs.CreativeTabController
import concerrox.minecraft.emiplusplus.id
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.gui.GuiGraphics


class CreativeTabsOverlay(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val onChanged: () -> Unit,
) {

    companion object {
        const val CREATIVE_TAB_HEIGHT = 18
        private val TEXTURE = id("textures/gui/buttons.png")
    }

    private val tabsPerPage
        get() = maxOf(1, (width - 36) / 18)

    private fun tabX(index: Int) = x + 18 + index * 18

    fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val context = EmiDrawContext.wrap(graphics)

        val tabs = CreativeTabController
            .visibleTabsForPage(tabsPerPage)
            .let { tabs ->
                tabs + List(tabsPerPage - tabs.size) { null }
            }

        val leftArrowX = x
        val tabBarX = x + 16
        val rightArrowX = tabBarX + tabsPerPage * 18 + 4

        RenderSystem.enableBlend()
        drawArrow(context, leftArrowX, y + 2, 0, mouseX, mouseY)

        context.drawTexture(TEXTURE, tabBarX, y + 2, 32, 0, 1, 16)
        context.drawTexture(TEXTURE, tabBarX + 1, y + 2, 32, 0, 1, 16)
        RenderSystem.enableBlend()

        for (index in tabs.indices) {
            RenderSystem.enableBlend()

            val tx = tabX(index)
            if (tx + 18 > rightArrowX - 2) {
                break
            }

            val tab = tabs[index]
            if (tab == null) {
                drawHiddenTab(context, tx)
                continue
            }

            val hovered = mouseX >= tx && mouseX < tx + 18 && mouseY >= y && mouseY < y + CREATIVE_TAB_HEIGHT
            val selected = CreativeTabController.selectedTab() == tab
            if (selected) {
                context.drawTexture(
                    TEXTURE, tx, y, 32, if (hovered) 50 else 32, 18, 18
                )
            } else {
                context.drawTexture(
                    TEXTURE, tx, y + 2, 32, if (hovered) 16 else 0, 18, 16
                )
            }

            context.push()
            context.matrices().translate(tx + 4F, y + 5F, 0F)
            context.matrices().scale(0.625F, 0.625F, 0.625F)
            graphics.renderFakeItem(tab.icon, 0, 0)
            context.pop()
            RenderSystem.enableBlend()
        }

        RenderSystem.enableBlend()
        context.drawTexture(TEXTURE, rightArrowX - 2, y + 2, 32, 0, 1, 16)
        context.drawTexture(TEXTURE, rightArrowX - 1, y + 2, 32, 0, 1, 16)
        drawArrow(context, rightArrowX, y + 2, 16, mouseX, mouseY)
        RenderSystem.enableBlend()
    }

    private fun drawHiddenTab(context: EmiDrawContext, x: Int) {
        context.drawTexture(TEXTURE, x, y + 16, 32, 14, 18, 2)
        context.fill(x, y + 2, 18, 14, 0xDB000000.toInt())
    }

    private fun drawArrow(
        context: EmiDrawContext, x: Int, y: Int, u: Int, mouseX: Int, mouseY: Int
    ) {
        val hovered = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16

        context.drawTexture(
            TEXTURE, x, y, u, if (hovered) 16 else 0, 16, 16
        )
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return false

        val leftArrowX = x
        val rightArrowX = x + 16 + tabsPerPage * 18 + 4

        if (mouseX >= leftArrowX && mouseX < leftArrowX + 16 && mouseY >= y + 2 && mouseY < y + 18) {
            CreativeTabController.previousPage(tabsPerPage)
            onChanged()
            return true
        }

        if (mouseX >= rightArrowX && mouseX < rightArrowX + 16 && mouseY >= y + 2 && mouseY < y + 18) {
            CreativeTabController.nextPage(tabsPerPage)
            onChanged()
            return true
        }

        CreativeTabController.visibleTabsForPage(tabsPerPage).forEachIndexed { index, tab ->
            val tx = tabX(index)

            if (mouseX >= tx && mouseX < tx + 18 && mouseY >= y && mouseY < y + CREATIVE_TAB_HEIGHT) {
                CreativeTabController.selectTab(tab)
                onChanged()
                return true
            }
        }

        return false
    }

    fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        val totalWidth = 16 + tabsPerPage * 18 + 4 + 16

        if (mouseX < x || mouseX > x + totalWidth || mouseY < y || mouseY > y + CREATIVE_TAB_HEIGHT) {
            return false
        }

        if (amount > 0) {
            CreativeTabController.previousPage(tabsPerPage)
        } else if (amount < 0) {
            CreativeTabController.nextPage(tabsPerPage)
        }

        onChanged()
        return true
    }
}