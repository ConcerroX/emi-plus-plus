package concerrox.emixx.content

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager
import com.lowdragmc.lowdraglib2.math.Rect
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.util.position
import concerrox.blueberry.ui.lowdraglib2.util.size
import concerrox.blueberry.ui.lowdraglib2.util.styleSheets
import concerrox.blueberry.ui.lowdraglib2.util.uiContent
import concerrox.emixx.content.headerfooter.HeaderFooterHelper
import concerrox.emixx.mixin.ScreenAccessor
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.screen.EmiScreenBase
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.neoforged.fml.loading.FMLEnvironment
import org.appliedenergistics.yoga.YogaPositionType

object EmiScreenAttachmentManager {

    internal const val EMI_TILE_ENTRY_SIZE = 18

    private lateinit var modularUI: ModularUI
    private lateinit var screen: Screen

    @JvmStatic
    var header = HeaderFooterHelper.createHeader()
        private set

    @JvmStatic
    var footer = HeaderFooterHelper.createFooter()
        private set

    var headerBounds: Rect = Rect.of(0, 0, 0, 0)
    var footerBounds: Rect = Rect.of(0, 0, 0, 0)

    private var tileColumnCount = 0
    private var tileRowCount = 0

    private lateinit var headerWidgetContainer: UIElement
    private lateinit var footerWidgetContainer: UIElement

    @JvmStatic
    fun onIndexScreenSpaceCreated(panel: EmiScreenManager.SidebarPanel) {
        val bounds = panel.bounds
        val l = bounds.x
        val w = bounds.width
        var t = bounds.y - header.visualHeight
        var h = header.contentHeight
        headerBounds = Rect.ofRelative(l, w, t, h)
        t = bounds.bottom() + footer.visualHeight - footer.contentHeight
        h = footer.contentHeight
        footerBounds = Rect.ofRelative(l, w, t, h)
    }

    @JvmStatic
    fun onMeasure() {
        val scn = EmiScreenBase.getCurrent().screen()
        if (!(::screen.isInitialized) || screen != scn) attach(scn)
        screen = scn
    }

    private fun attach(screen: Screen) {
        // Recreate in dev env every time so we can hotswap it
        if (!(::modularUI.isInitialized) || !FMLEnvironment.production) recreateView()

        modularUI.setScreenAndInit(screen)
        (screen as ScreenAccessor).addRenderableWidgetExternal(modularUI.widget)
    }

    private fun recreateView() {
        println("OR")
        // TODO: do not recreate header and footer
        header = HeaderFooterHelper.createHeader()
        footer = HeaderFooterHelper.createFooter()
        modularUI = createModularUI()
    }

//    private fun UIScope.createIndexSidebarHeaderOrFooterWidget() = when (headerType) {
//        else -> {
//            Element(layout = { flexDirection(YogaFlexDirection.ROW) }) {
//                Element(::TabView).apply {
//                    addTab(Tab().apply {
//                        setText("HELLO")
//                    }, UIElement())
//                    addTab(Tab().apply {
//                        setText("HELLO")
//                    }, UIElement())
//                    addTab(Tab().apply {
//                        setText("HELLO")
//                    }, UIElement())
//                    addTab(Tab().apply {
//                        setText("HELLO")
//                    }, UIElement())
//                    addTab(Tab().apply {
//                        setText("HELLO")
//                    }, UIElement())
//
//                    tabScroller.viewContainer.layout { it.paddingAll(0f) }
//                    tabContentContainer.isVisible = false
//                }
//            }
//        }
//    }

    private fun createModularUI() = uiContent(styleSheets = styleSheets(StylesheetManager.MC)) {
        Element(layout = {
            widthPercent(100f)
            heightPercent(100f)
        }) {
            Element(::headerWidgetContainer, layout = {
                val bounds = headerBounds
                position(bounds.left, bounds.up)
                size(bounds.width, bounds.height)
                positionType(YogaPositionType.ABSOLUTE)
            }) {
                header.createUI(this, headerBounds)
            }
        }
    }

    @JvmStatic
    fun onRender(guiGraphics: GuiGraphics) {
        if (EmiConfig.highlightExclusionAreas) {
            val hb = headerBounds
            guiGraphics.fill(hb.left, hb.up, hb.right, hb.down, 0x30FFFF00)
            val fb = footerBounds
            guiGraphics.fill(fb.left, fb.up, fb.right, fb.down, 0x30FFFF00)
        }
    }

}