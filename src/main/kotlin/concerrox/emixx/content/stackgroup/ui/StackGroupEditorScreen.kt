package concerrox.emixx.content.stackgroup.ui

import concerrox.emixx.oreui.OreUi
import concerrox.emixx.oreui.component.ScrollView
import concerrox.emixx.oreui.component.TitleBar
import concerrox.emixx.oreui.component.listitem.InputListItem
import concerrox.emixx.oreui.view.OreUiScreen
import concerrox.emixx.oreui.view.addAutoWidget
import concerrox.emixx.id
import concerrox.emixx.text
import concerrox.emixx.util.fastBlitNineSlicedSprite
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class StackGroupEditorScreen : OreUiScreen(text("gui", "edit_stack_group")) {

    companion object {
        private val NAVIGATION_VIEW_BACKGROUND = id("navigation_view/background")
    }

    override val viewModel = StackGroupEditorViewModel()

    private val contentTop = TitleBar.TITLE_BAR_HEIGHT + OreUi.SPACING_SMALL - 1

    private val titleBar = TitleBar(title, ::onClose)
    private val editorScroll = ScrollView()

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick)

        guiGraphics.fastBlitNineSlicedSprite(
            NAVIGATION_VIEW_BACKGROUND, 0, contentTop, 128, height - contentTop - OreUi.SPACING_SMALL
        )
    }

    override fun onCreateView() {
        addAutoWidget(titleBar)
        addAutoWidget(editorScroll)

        titleBar.width = width
        editorScroll.setPosition(128 + OreUi.SPACING_MEDIUM, contentTop - OreUi.SPACING_SMALL)
        editorScroll.setSize(256, height - contentTop + OreUi.SPACING_SMALL)
        editorScroll.setItems(*List(20) {
            InputListItem(Component.literal("Identifier"))
        }.toTypedArray())

        titleBar.arrangeElements()
    }

    override fun onBindData() {

    }

    override fun onCreated() {

    }

}
