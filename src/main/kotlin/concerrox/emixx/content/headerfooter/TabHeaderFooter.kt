package concerrox.emixx.content.headerfooter

import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.elements.Tab
import com.lowdragmc.lowdraglib2.gui.ui.elements.TabView
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext
import com.lowdragmc.lowdraglib2.math.Rect
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.size
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.registry.ModSpriteTextures
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.config.SidebarSide
import dev.emi.emi.config.SidebarTheme

class TabHeaderFooter(isHeader: Boolean) : HeaderFooter(isHeader) {

    companion object {
        private val isVanillaThemeEnabled
            get(): Boolean {
                val theme = when (EmiConfig.searchSidebar) {
                    SidebarSide.NONE -> null
                    SidebarSide.LEFT -> EmiConfig.leftSidebarTheme
                    SidebarSide.RIGHT -> EmiConfig.rightSidebarTheme
                    SidebarSide.TOP -> EmiConfig.topSidebarTheme
                    SidebarSide.BOTTOM -> EmiConfig.bottomSidebarTheme
                }
                return theme == SidebarTheme.VANILLA
            }
    }

    private val indexSidebarTabButtonSize get() = EmiPlusPlusConfig.indexSidebarTabButtonSize.get()

    override val contentHeight: Int get() = indexSidebarTabButtonSize * 20 / 18

    // In vanilla theme, the bottom border (3px) is not in the bounds?
    override val gap: Int get() = if (!isHeader && isVanillaThemeEnabled) 3 else 0

    override fun createUI(scope: UIScope, bounds: Rect): Unit = scope.run {
        val width = bounds.width
        val itemWidth = indexSidebarTabButtonSize
        val itemHeight = itemWidth * 20 / 18
        val itemCount = width / itemWidth

        Element(::TabView).run {
            removeChild(tabContentContainer)
            tabHeaderContainer.layout { it.paddingHorizontal(0f) }
            repeat(itemCount) {
                addTab(HeaderFooterTab().apply {
                    layout { it.size(itemWidth, itemHeight) }
                    tabStyle {
                        it.baseTexture(ModSpriteTextures.TAB_BACKGROUND_MODERN)
                        it.hoverTexture(ModSpriteTextures.TAB_BACKGROUND_MODERN_HOVERED)
                        it.selectedTexture(ModSpriteTextures.TAB_BACKGROUND_MODERN_SELECTED)
                    }
                }, UIElement())
            }
        }
    }

    class HeaderFooterTab : Tab() {
        override fun drawBackgroundOverlay(guiContext: GUIContext) {
            super.drawBackgroundOverlay(guiContext)
            val pose = guiContext.graphics.pose
            pose.pushPose()

            val verticalOffset = (sizeWidth / 18f) * 1f
            val targetSize = sizeWidth * 12f / 18f
            val sc = targetSize / 16f
            val x = positionX + (sizeWidth - targetSize) / 2f
            val y = positionY + (sizeHeight - targetSize) / 2f + verticalOffset

            pose.translate(x, y, 0f)
            pose.scale(sc, sc, 1f)

//            DrawerHelper.drawItemStack(
//                guiContext.graphics, Items.GRASS_BLOCK.defaultInstance,
//                0, 0, 0xFFFFFF, null,
//            )
            pose.popPose()
        }
    }

}