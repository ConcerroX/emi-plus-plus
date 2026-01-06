package concerrox.emixx.content.stackgroup.gui

import concerrox.emixx.content.stackgroup.StackGroupManagerV2
import concerrox.emixx.oreui.OreUi
import concerrox.emixx.oreui.component.TextButton
import concerrox.emixx.oreui.component.TitleBar
import concerrox.emixx.oreui.component.ToggleButtonGroup
import concerrox.emixx.oreui.view.OreUiScreen
import concerrox.emixx.oreui.view.ScreenManager
import concerrox.emixx.oreui.view.addAutoWidget
import concerrox.emixx.text
import net.minecraft.client.gui.layouts.LinearLayout

class StackGroupConfigScreenV2 : OreUiScreen(text("gui", "stack_group_config")) {

    companion object {
        private val PREBUILT_TAB = ToggleButtonGroup.Item(text("gui", "prebuilt"))
        private val CUSTOM_TAB = ToggleButtonGroup.Item(text("gui", "custom"))
        private val TABS = listOf(PREBUILT_TAB, CUSTOM_TAB)
    }

    private val titleTabContainer = LinearLayout.vertical().apply {
        defaultCellSetting().alignHorizontallyCenter()
        spacing(OreUi.SPACING_SMALL - 1) // 1px translucent shadow
    }
    private val titleBar = titleTabContainer.addChild(TitleBar(title, ::onClose))
    private val tabBar = titleTabContainer.addChild(ToggleButtonGroup(TABS))
    private val createButton = TextButton(text("gui", "create"), buttonStyle = TextButton.Style.HERO) {
        ScreenManager.pushScreen(StackGroupEditorScreen())
    }
    private val createButton2 = TextButton(text("gui", "create"), buttonStyle = TextButton.Style.SECONDARY) {}
    private val gridView = StackGroupConfigGridViewV2()

    override val viewModel = StackGroupConfigViewModelV2()

    override fun onCreateView() {
        addAutoWidget(tabBar)
        addAutoWidget(titleBar)
        addAutoWidget(createButton)
        addAutoWidget(createButton2)
        addAutoWidget(gridView)

        val columnCount = width / (StackGroupConfigGridViewV2.ITEM_VIEW_WIDTH + OreUi.SPACING_MEDIUM)
        val containerWidth =
            columnCount * StackGroupConfigGridViewV2.ITEM_VIEW_WIDTH + (columnCount - 1) * OreUi.SPACING_MEDIUM + StackGroupConfigGridViewV2.SCROLLBAR_WIDTH
        val containerLeft = (width - containerWidth) / 2
        titleBar.width = width
        tabBar.width = containerWidth
        gridView.width = containerWidth

        titleTabContainer.arrangeElements()
        titleBar.arrangeElements()
        tabBar.arrangeElements()

        createButton.setPosition(tabBar.right - createButton.width, tabBar.bottom + OreUi.SPACING_SMALL)
        createButton2.setPosition(
            createButton.x - OreUi.SPACING_SMALL - createButton2.width, tabBar.bottom + OreUi.SPACING_SMALL
        )

        gridView.apply {
            val height = this@StackGroupConfigScreenV2.height - createButton.bottom - OreUi.SPACING_SMALL
            updateSizeAndPosition(containerWidth, height, this@StackGroupConfigScreenV2.height - height)
            x = containerLeft
            this.columnCount = columnCount
        }
    }

    override fun onBindData() {
        viewModel.prebuiltStackGroups.observe {
            gridView.data = it
        }
    }

    override fun onCreated() {
        StackGroupManagerV2.collectAllContentsAsync {
            viewModel.prebuiltStackGroups.value = it
        }
    }

}