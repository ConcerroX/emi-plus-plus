package concerrox.emixx.content.stackgroup.editor

import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label
import concerrox.blueberry.ui.binding.bindLiveData
import concerrox.blueberry.ui.binding.liveData
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.Label
import concerrox.blueberry.ui.lowdraglib2.LayoutBuilder
import concerrox.blueberry.ui.lowdraglib2.Row
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.neo.OreSprites
import concerrox.blueberry.ui.neo.component.NeoButton
import concerrox.blueberry.ui.neo.component.NeoDialog
import concerrox.blueberry.ui.neo.component.NeoDialogActionUIScopeImpl
import concerrox.blueberry.ui.neo.component.NeoSpinner
import concerrox.blueberry.ui.neo.component.NeoTextField
import concerrox.blueberry.ui.neo.component.NeoVerticalScrollView
import concerrox.blueberry.ui.neo.component.preference.NeoPreference
import concerrox.blueberry.ui.neo.component.preference.NeoTextFieldPreference
import concerrox.emixx.content.stackgroup.data.AbstractStackGroup
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.content.stackgroup.data.RegistryTokens
import concerrox.emixx.content.stackgroup.editor.component.GroupingRuleTypeSpinner
import concerrox.emixx.content.stackgroup.editor.component.StackPreview
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import concerrox.emixx.id
import concerrox.emixx.registry.ModLang
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import dev.vfyjxf.taffy.style.AlignItems
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class StackGroupRuleDialogFragment(private val onSave: (GroupingRule) -> Unit) {

    companion object {
        private val PAGE_WIDTH = 1 + 12 + StackPreview.calculateWidth(9) + 12 + 1 + 4 + StackPreview.calculateWidth(9)
        private val DIALOG_WIDTH = 8 + PAGE_WIDTH + 8
    }

    private val viewModel = StackGroupRuleViewModel()

    @Deprecated("")
    fun uiContent(scope: UIScope) = scope.run {
        NeoDialog(
            title = ModLang.editGroupingRule,
            actionContent = {
                DialogAction() // TODO: migrate to blueberry lib
            },
        ) {
            DialogContent()
        }
    }

    private fun NeoDialogActionUIScopeImpl.DialogAction(): UIElement {
        val dialog = parent
        return Row(layout = { gapAll(2f) }) {
            NeoButton(
                text = ModLang.cancel,
                onClick = { dialog.close() },
                layout = { flexFill() },
            )
            NeoButton(
                text = ModLang.save,
                variant = NeoButton.Variant.PRIMARY,
                layout = { flexFill() },
                onClick = {
                    onSave(viewModel.rule!!)
                    dialog.close()
                },
            )
        }
    }

    private fun UIScope.DialogContent() =
        Row(layout = { gapAll(4f).paddingHorizontal(8f).flexGrow(0f).flexShrink(1f) }) {
            LeftPanel()
            RightPanel()
        }

    private fun UIScope.Panel(layout: LayoutBuilder = null, content: UIScope.() -> Unit) = Column(
        layout = {
            layout?.invoke(this)
            paddingAll(1f)
        },
        styles = { background(OreSprites.PAGE_BACKGROUND) },
    ) {
        content()
    }

    private fun UIScope.LeftPanel() = NeoVerticalScrollView(
        layout = { maxHeightPercent(100f) },
        viewContainerLayout = { gapAll(4f).paddingVertical(8f) },
    ) {
        Panel {
            NeoPreference(ModLang.stackType) {
                NeoSpinner(
                    viewModel.registryToken,
                    RegistryTokens.listTokens(),
                    { it.translationKey.key },
                    { marginHorizontal(-1f) },
                )
            }
            NeoPreference(ModLang.ruleType) {
                GroupingRuleTypeSpinner(viewModel.ruleType)
            }.apply {
                viewModel.ruleType.observe { description = it.descriptionKey.asComponent() }
            }
        }

        Panel {
            NeoTextFieldPreference(ModLang.ruleNotation).apply {
                textField.isFocusable = false
                textField.bindLiveData(viewModel.notation)
            }
            NeoPreference(ModLang.matchedStacks) {
                StackPreview(
                    viewModel.previewStacks,
                    adaptiveHeight = true,
                )
            }
        }
    }.apply {
        scrollbar.layout.marginVertical(8f)
    }

    private fun UIScope.RightPanel() = Panel(layout = { marginVertical(8f).alignSelf(AlignItems.START) }) {
        var pageCounter: Label? = null
        var pageCount = 0
        NeoPreference(layout = { paddingHorizontal(8f) }) {
            Row(layout = { alignItems(AlignItems.CENTER) }) {
                pageCounter = Label(CommonComponents.EMPTY, textStyles = { adaptiveWidth(true).textShadow(false) })
                NeoTextField(layout = { flexFill().marginLeft(6f) }).apply {
                    textFieldStyle.placeholder(Component.literal("Search stacks…"))
                }
            }
        }.apply {
            titleElement.setDisplay(false)
        }
        NeoPreference(layout = { paddingHorizontal(8f) }) {
            val stacks = liveData { EmiStackList.filteredStacks }
            val sgs = object : AbstractStackGroup(id(""), "", false) {
                override fun match(stack: EmiStack) = TODO("Not yet implemented")
                override fun loadContent() = TODO("Not yet implemented")
            }
            stacks.observe { println("UPD") }
            StackPreview(
                data = stacks,
                rows = 12,
                onPickup = { result ->
                    if (result !is EmiStack) return@StackPreview
                    viewModel.previewStacks.value = EmiStackList.filteredStacks.filter {
                        it is EmiStack && it.id == result.id
                    }
                    viewModel.notation.value = "&item:${result.id}"
                    stacks.value = EmiStackList.filteredStacks.map {
                        // TODO: wrapper w/o real groups
                        if (it is EmiStack && it.id == result.id) GroupedEmiStackWrapper(
                            it,
                            sgs
                        ) else it
                    }
                },
                onPageCountChange = { pageCount = it },
                onPageChange = { pageCounter?.setText("$it/$pageCount") },
            )
        }.apply {
            titleElement.setDisplay(false)
        }
    }

//    private fun UIScope.ContentTabPager() = NeoTabPager(
//        layout = { width(DIALOG_WIDTH).paddingTop(8f).paddingHorizontal(8f).flexGrow(0f).flexShrink(1f) },
//    ) {
//        RuleTabItem(translatable("Tag"), leftPanelMainContent = {
//            NeoPreference(title = translatable("Tag ID")) {
//                NeoSearchBar(searcher = viewModel.tagSearcherById, data = viewModel.selectedTag).apply {
//                    setCandidateUIProvider(NeoSearchBar.textUiProvider { it?.location.toString() })
//                }
//            }
//        }) {
//
//        }

//        RuleTabItem(translatable("Stack"), leftPanelMainContent = {
//            NeoSwitchPreference(
//                title = translatable("Match data components"),
//                description = translatable("Match data of stacks exactly")
//            ).apply {
//                switch.bindLiveData(viewModel.matchDataComponents)
//            }
//        }) {
//            StackPicker(
//                rows = 12,
//                layout = { minHeightPercent(100f) },
//                styles = { background(OreSprites.PAGE_BACKGROUND) },
//                onPickup = {
//                    viewModel.pickedStack.value = it
//                    pickedStacks.clear()
//                    pickedStacks.add(it)
//                }) {
//                NeoTextField().apply {
//                    bindLiveData(viewModel.searchKeyword)
//                    textFieldStyle.placeholder(Component.literal("Search…"))
//                }
//            }.apply {
//                bindDataSource(LiveDataSource(viewModel.availableStacks) { it })
//            }
//        }
//
//        RuleTabItem(translatable("Regex"), leftPanelMainContent = {
//
//        }) {
//
//        }
//    }.apply {
//        setOnTabSelected { viewModel.updateType(it.siblingIndex) }
//    }

//    private fun TabUIScopeImpl.RuleTabItem(
//        title: Component, leftPanelMainContent: UIScope.() -> Unit, rightPanel: UIScope.() -> Unit
//    ) = NeoTabItem(
//        text = title,
//        scrollbarDisplay = ScrollbarDisplay.Never,
//        viewContainerLayout = { flexRow().gapAll(4f).paddingBottom(8f).width(PAGE_WIDTH) },
//    ) {
//        Column(layout = { gapAll(4f).flexShrink(1f) }) {
//            Column(
//                layout = { paddingAll(1f) },
//                styles = { background(OreSprites.PAGE_BACKGROUND) },
//            ) {
//                NeoPreference(title = translatable("Stack type")) {
//                    NeoSpinner(
//                        viewModel.tokenSerializationType, items = viewModel.tokenSerializationTypes
//                    )
//                }
//                leftPanelMainContent()
//            }
//            Column(
//                layout = { paddingAll(1f) },
//                styles = { background(OreSprites.PAGE_BACKGROUND) },
//            ) {
//                NeoTextFieldPreference(title = translatable("Rule notation")).apply {
//                    textField.isFocusable = false
//                    textField.bindLiveData(viewModel.notation)
//                }
//                NeoPreference(title = translatable("Stack preview")) {
//                    StackPreview(
//                        viewModel.previewStacks,
//                        adaptiveHeight = true,
//                        styles = { background(OreSprites.PANEL_BACKGROUND) },
//                    )
//                }
//            }
//        }
//        rightPanel()
//    }.apply {
//        scrollView!!.scrollbar.layout.paddingBottom(8f)
//    }

}