package concerrox.emixx.content.stackgroup.editor

import concerrox.blueberry.ui.binding.LiveData
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.LazyRow
import concerrox.blueberry.ui.lowdraglib2.util.fillMaxSize
import concerrox.blueberry.ui.lowdraglib2.util.fillMaxWidth
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.lowdraglib2.util.uiContent
import concerrox.blueberry.ui.neo.StyleSheets
import concerrox.blueberry.ui.neo.component.NeoButton
import concerrox.blueberry.ui.neo.component.NeoListItem
import concerrox.blueberry.ui.neo.component.NeoTabItem
import concerrox.blueberry.ui.neo.component.NeoTabPager
import concerrox.blueberry.ui.neo.component.NeoTitleBar
import concerrox.blueberry.ui.neo.component.TabUIScopeImpl
import concerrox.blueberry.ui.neo.style.Dimensions
import concerrox.blueberry.ui.screen.ScreenManager
import concerrox.blueberry.ui.screen.UIScreen
import concerrox.emixx.registry.ModTranslationKeys
import dev.vfyjxf.taffy.style.AlignItems
import net.minecraft.network.chat.Component

class StackGroupConfigScreen : UIScreen(ModTranslationKeys.Config.STACK_GROUP_CONFIG.asComponent()) {

    override val viewModel = StackGroupConfigViewModel()
    override val modularUi = uiContent(StyleSheets.neo()) {
        Element(layout = { fillMaxSize().alignItems(AlignItems.CENTER) }) {
            NeoTitleBar(title)
            NeoTabPager(layout = { flexFill().fillMaxWidth() }) {
                StackGroupTabItem(
                    title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_TAB_ALL.asComponent(),
                    data = viewModel.stackGroups,
                )
                StackGroupTabItem(
                    title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_TAB_APPLICABLE.asComponent(),
                    data = viewModel.stackGroups,
                )
                StackGroupTabItem(
                    title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_TAB_NOT_APPLICABLE.asComponent(),
                    data = viewModel.stackGroups,
                )
            }
        }
    }

    private fun TabUIScopeImpl.StackGroupTabItem(
        title: Component, data: LiveData<List<StackGroupConfigViewModel.StackGroupUiState>>
    ) {
        NeoTabItem(
            text = title,
            layout = { gapAll(Dimensions.ContentMarginSmall) },
        ) {
            NeoButton(
                text = ModTranslationKeys.Config.STACK_GROUP_CONFIG_CREATE.asComponent(),
                variant = NeoButton.Variant.PRIMARY,
                layout = { maxWidth(128f) },
                onClick = { ScreenManager.pushScreen(StackGroupEditorScreen()) })
            LazyRow(
                data = data,
                layout = { gapAll(Dimensions.ContentMarginMedium) },
            ) {
                Column(layout = { width(NeoTabPager.TAB_ITEM_CARD_WIDTH) }) {
                    Element(::StackPreview, layout = { height(52f) }).apply {
                        stackGroups = it.previewStacks
                    }
                    NeoListItem(title = it.nameComponent, description = it.idComponent)
                }
            }
        }
    }

    override fun onBindData() {}

    override fun onCreated() {
        viewModel.loadStackGroups()
    }

}