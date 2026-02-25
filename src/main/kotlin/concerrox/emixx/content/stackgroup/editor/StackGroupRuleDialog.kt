package concerrox.emixx.content.stackgroup.editor

import concerrox.blueberry.ui.binding.bindLiveData
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.Row
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.style.ScrollbarDisplay
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.lowdraglib2.util.flexRow
import concerrox.blueberry.ui.neo.OreSprites
import concerrox.blueberry.ui.neo.component.NeoButton
import concerrox.blueberry.ui.neo.component.NeoDialog
import concerrox.blueberry.ui.neo.component.NeoSearchBar
import concerrox.blueberry.ui.neo.component.NeoSpinner
import concerrox.blueberry.ui.neo.component.NeoTabItem
import concerrox.blueberry.ui.neo.component.NeoTabPager
import concerrox.blueberry.ui.neo.component.NeoTextField
import concerrox.blueberry.ui.neo.component.TabUIScopeImpl
import concerrox.blueberry.ui.neo.component.preference.NeoPreference
import concerrox.blueberry.ui.neo.component.preference.NeoSwitchPreference
import concerrox.blueberry.ui.neo.component.preference.NeoTextFieldPreference
import concerrox.emixx.content.stackgroup.data.EmiStackGroup
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import concerrox.emixx.id
import concerrox.emixx.text
import dev.emi.emi.EmiPort.translatable
import dev.emi.emi.registry.EmiStackList
import net.minecraft.network.chat.Component

private val ITEM_WIDTH = 1 + 12 + StackPreview.calculateWidth(9) + 12 + 1 + 4 + StackPreview.calculateWidth(9)
private val DIALOG_WIDTH = 8 + ITEM_WIDTH + 8

// TODO: create a class

fun UIScope.StackGroupRuleDialog(onSave: (GroupingRule) -> Unit) {

    val viewModel = StackGroupRuleViewModel()

    NeoDialog(
        title = text("Edit grouping rule"),
        actionContent = {
            val dialog = parent
            Row(layout = { gapAll(2f) }) {
                NeoButton(
                    text = translatable("Cancel"),
                    onClick = { dialog.close() },
                    layout = { flexFill() },
                )
                NeoButton(
                    text = translatable("Save changes"),
                    variant = NeoButton.Variant.PRIMARY,
                    layout = { flexFill() },
                    onClick = {
                        onSave(viewModel.rule!!)
                        dialog.close()
                    },
                )
            }
        },
    ) {
        NeoTabPager(
            layout = { width(DIALOG_WIDTH).paddingTop(8f).paddingHorizontal(8f).flexGrow(0f).flexShrink(1f) },
        ) {

            RuleTabItem(translatable("Tag"), viewModel = viewModel, leftPanelMainContent = {
                NeoPreference(title = translatable("Tag ID")) {
                    NeoSearchBar(searcher = viewModel.tagSearcherById, data = viewModel.selectedTag).apply {
                        setCandidateUIProvider(NeoSearchBar.textUiProvider { it?.location.toString() })
                    }
                }
            }) {

            }

            RuleTabItem(translatable("Stack"), viewModel = viewModel, leftPanelMainContent = {
                NeoSwitchPreference(
                    title = translatable("Match data components"),
                    description = translatable("Match data of stacks exactly")
                )
            }) {
                StackPicker(
                    rows = 12,
                    layout = { minHeightPercent(100f) },
                    styles = { background(OreSprites.PAGE_BACKGROUND) },
                ) {
                    NeoTextField().apply {
                        bindLiveData(viewModel.searchKeyword)
                        textFieldStyle.placeholder(Component.literal("Search…"))
                    }
                }.apply {
                    val g = EmiStackGroup(id("t"), setOf())
                    stackGroups = EmiStackList.filteredStacks.map {
                        if (it.id.toString().contains("oak")) {
                            GroupedEmiStackWrapper(it, g)
                        } else it
                    }
                }
            }

            RuleTabItem(translatable("Regex"), viewModel = viewModel, leftPanelMainContent = {

            }) {

            }

        }.apply {
            setOnTabSelected { viewModel.updateType(it.siblingIndex) }
        }
    }.apply {
        dialogView.layout.maxHeightPercent(90f)
    }

}

private fun TabUIScopeImpl.RuleTabItem(
    title: Component,
    viewModel: StackGroupRuleViewModel,
    leftPanelMainContent: UIScope.() -> Unit,
    rightPanel: UIScope.() -> Unit
) {
    NeoTabItem(
        text = title,
        scrollbarDisplay = ScrollbarDisplay.Never,
        viewContainerLayout = { flexRow().gapAll(4f).paddingBottom(8f).width(ITEM_WIDTH) },
    ) {
        Column(layout = { gapAll(4f).flexShrink(1f) }) {
            Column(
                layout = { paddingAll(1f) },
                styles = { background(OreSprites.PAGE_BACKGROUND) },
            ) {
                NeoPreference(title = translatable("Stack type")) {
                    NeoSpinner(viewModel.tokenSerializationType, items = viewModel.tokenSerializationTypes)
                }
                leftPanelMainContent()
            }
            Column(
                layout = { paddingAll(1f) },
                styles = { background(OreSprites.PAGE_BACKGROUND) },
            ) {
                NeoTextFieldPreference(title = translatable("Rule notation")).apply {
                    textField.isFocusable = false
                    textField.bindLiveData(viewModel.notation)
                }
                NeoPreference(title = translatable("Stack preview")) {
                    StackPreview(
                        viewModel.previewStacks,
                        adaptiveHeight = true,
                        styles = { background(OreSprites.PANEL_BACKGROUND) },
                    )
                }
            }
        }
        rightPanel()
    }.apply {
        scrollView.scrollbar.layout.paddingBottom(8f)
    }
}

//private fun UIScope.ResultPreviewPanel(viewModel: StackGroupRuleViewModel, previewColumns: Int = 8) {
//    Column(
//        layout = { paddingAll(1f) },
//        styles = { background(OreSprites.PAGE_BACKGROUND) },
//    ) {
//        NeoTextFieldPreference(title = translatable("Rule notation")).apply {
//            textField.isFocusable = false
//            textField.bindLiveData(viewModel.notation)
//        }
//        NeoPreference(title = translatable("Stack preview")) {
//            StackGroupPreview(
//                viewModel.previewStacks, previewColumns,
//                styles = { background(OreSprites.PANEL_BACKGROUND) },
//            )
//        }
//    }
//}