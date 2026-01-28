package concerrox.emixx.content.stackgroup.editor

import com.lowdragmc.lowdraglib2.gui.ui.elements.Label
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.Label
import concerrox.blueberry.ui.lowdraglib2.LazyRow
import concerrox.blueberry.ui.lowdraglib2.util.fillMaxSize
import concerrox.blueberry.ui.lowdraglib2.util.uiContent
import concerrox.blueberry.ui.neo.StyleSheets
import concerrox.blueberry.ui.neo.component.NeoButton
import concerrox.blueberry.ui.neo.component.NeoTabItem
import concerrox.blueberry.ui.neo.component.NeoTabPager
import concerrox.blueberry.ui.neo.component.NeoTitleBar
import concerrox.blueberry.ui.neo.style.Dimensions
import concerrox.blueberry.ui.screen.UIScreen
import concerrox.emixx.content.stackgroup.EmiPlusPlusStackGroups
import concerrox.emixx.content.stackgroup.StackGroupManagerV2
import concerrox.emixx.registry.ModTranslationKeys
import concerrox.emixx.util.text
import org.appliedenergistics.yoga.YogaAlign

class StackGroupConfigScreen : UIScreen(ModTranslationKeys.Config.STACK_GROUP_CONFIG.asComponent()) {

    private lateinit var loadingLabel: Label
    private lateinit var loadedLabel: Label

    override val viewModel = StackGroupConfigViewModel()
    override val modularUI = uiContent(StyleSheets.neo()) {
        Element(layout = { fillMaxSize().setAlignItems(YogaAlign.CENTER) }) {
            NeoTitleBar(title)
            NeoTabPager {
                NeoTabItem(text = ModTranslationKeys.Config.STACK_GROUP_CONFIG_TAB_ALL.asComponent()) {
                    NeoButton(text = ModTranslationKeys.Config.STACK_GROUP_CONFIG_CREATE.asComponent())
                    LazyRow(data = viewModel.data, layout = { gapAll(Dimensions.ContentMarginMedium) }) {
                        NeoButton(text = ModTranslationKeys.Config.STACK_GROUP_CONFIG.asComponent())
                    }
                }
                NeoTabItem(text = ModTranslationKeys.Config.STACK_GROUP_CONFIG_TAB_APPLICABLE.asComponent()) {
                    Label(text("NNNNN"))
                }
                NeoTabItem(text = ModTranslationKeys.Config.STACK_GROUP_CONFIG_TAB_NOT_APPLICABLE.asComponent()) {
                    Label(text("NNNNN"))
                }
            }
        }
    }

    override fun onBindData() {
        viewModel.loadingStatus.observe {
            when (it) {
                StackGroupManagerV2.LoadingStatus.DONE -> {
//                    viewModel.data.value = listOf(1, 2, 3, 4)
                }
                else -> {
                }
            }
        }
    }

    override fun onCreated() {
        EmiPlusPlusStackGroups.reload()
        StackGroupManagerV2.reload()
    }

}