package concerrox.emixx.content.stackgroup.editor

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import concerrox.blueberry.ui.binding.bindLiveData
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.Label
import concerrox.blueberry.ui.lowdraglib2.LazyColumn
import concerrox.blueberry.ui.lowdraglib2.Row
import concerrox.blueberry.ui.lowdraglib2.ScrollView
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.addContent
import concerrox.blueberry.ui.lowdraglib2.util.addContentAt
import concerrox.blueberry.ui.lowdraglib2.util.fillMaxSize
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.lowdraglib2.util.uiContent
import concerrox.blueberry.ui.neo.OreSprites
import concerrox.blueberry.ui.neo.StyleSheets
import concerrox.blueberry.ui.neo.component.NeoButton
import concerrox.blueberry.ui.neo.component.NeoClickableView
import concerrox.blueberry.ui.neo.component.NeoListItem
import concerrox.blueberry.ui.neo.component.NeoTitleBar
import concerrox.blueberry.ui.neo.component.preference.NeoSubheader
import concerrox.blueberry.ui.neo.component.preference.NeoSwitchPreference
import concerrox.blueberry.ui.neo.component.preference.NeoTextFieldPreference
import concerrox.blueberry.ui.screen.UIScreen
import concerrox.emixx.content.stackgroup.data.AbstractStackGroup
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.registry.ModSprites
import concerrox.emixx.registry.ModTranslationKeys
import dev.emi.emi.EmiPort.translatable
import dev.vfyjxf.taffy.style.AlignItems
import org.appliedenergistics.yoga.YogaAlign

class StackGroupEditorScreen(editingStackGroup: EmiStackGroupV2? = null) : UIScreen(
    title = if (editingStackGroup != null) {
        ModTranslationKeys.Config.STACK_GROUP_CONFIG_EDIT_STACK_GROUP.asComponent()
    } else {
        ModTranslationKeys.Config.STACK_GROUP_CONFIG_CREATE_STACK_GROUP.asComponent()
    }
) {

    companion object {
        private val LEFT_PANEL_WIDTH = StackPreview.calculateWidth(9) + 6f * 2
    }

    private val isEditing = editingStackGroup != null

    override val viewModel = StackGroupEditorViewModel(editingStackGroup)
    override val modularUi = uiContent(StyleSheets.neo()) {
        Column(layout = { fillMaxSize().minWidth(128f).setAlignItems(YogaAlign.CENTER) }) {
            NeoTitleBar(title)
            Row(layout = { flexFill().gapAll(6f) }) {
                LeftPanel()
                RightPanel()
            }
        }
    }

    private fun UIScope.LeftPanel() {
        Column(
            layout = { width(LEFT_PANEL_WIDTH).marginVertical(4f).paddingAll(6f).gapAll(4f) },
            styles = { background(OreSprites.PANEL_BACKGROUND) },
        ) {
            NeoButton(
                text = if (isEditing) {
                    ModTranslationKeys.Config.STACK_GROUP_CONFIG_SAVE.asComponent()
                } else {
                    ModTranslationKeys.Config.STACK_GROUP_CONFIG_CREATE.asComponent()
                },
                variant = NeoButton.Variant.HERO,
            )

            NeoButton(
                text = if (isEditing) {
                    ModTranslationKeys.Config.STACK_GROUP_CONFIG_DELETE.asComponent()
                } else {
                    ModTranslationKeys.Config.STACK_GROUP_CONFIG_DISCARD.asComponent()
                },
                variant = NeoButton.Variant.DESTRUCTIVE,
            )

            this.StackPreview(
                data = viewModel.previewStacks,
                columns = 9,
                layout = { flexFill() },
                styles = { background(OreSprites.PAGE_BACKGROUND) },
            )
        }
    }

    private fun UIScope.RightPanel() {
        ScrollView(
            layout = { width(LEFT_PANEL_WIDTH * 2) },
            viewContainerLayout = { paddingAll(1f).marginVertical(4f) },
            viewContainerStyles = { background(OreSprites.PAGE_BACKGROUND) },
        ) {

            NeoSwitchPreference(
                title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_ENABLE_STACK_GROUP.asComponent(),
                description = ModTranslationKeys.Config.STACK_GROUP_CONFIG_ENABLE_STACK_GROUP_DESC.asComponent()
            ).apply {
                switch.bindLiveData(viewModel.stackGroupEnabled)
            }
            NeoTextFieldPreference(
                title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_STACK_GROUP_NAME.asComponent(),
            ).apply {
                textField.bindLiveData(viewModel.stackGroupName)
            }
            NeoTextFieldPreference(
                title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_STACK_GROUP_ID.asComponent(),
            ).apply {
                textField.setResourceLocationOnly().bindLiveData(viewModel.stackGroupId)
            }
            NeoTextFieldPreference(
                title = ModTranslationKeys.Config.STACK_GROUP_CONFIG_STACK_FILENAME.asComponent(),
            ).apply {
                textField.isFocusable = false
                viewModel.stackGroupId.observe { textField.text = AbstractStackGroup.buildConfigFilename(it) }
            }

            NeoSubheader(
                text = ModTranslationKeys.Config.STACK_GROUP_CONFIG_GROUPING_RULES.asComponent(),
            )
            NeoButton(
                text = translatable("Add rule"),
                layout = { marginVertical(6f).marginHorizontal(12f) },
                onClick = { StackGroupRuleDialog(onSave = { viewModel.addRule(it) }) },
            )

            LazyColumn(
                data = viewModel.stackGroupRuleUiStates,
                layout = { marginHorizontal(12f).marginBottom(8f) },
            ) {
                RuleListItem(it)
            }

//            Column(layout = { marginHorizontal(6f).marginBottom(6f) }) {
//                NeoListItem(
//                    title = translatable("方块标签 §6石头"),
//                    description = translatable("#block:c:stones"),
//                    layout = { marginBottom(-1f) },
//                ).addContentAt(0) {
//                    // TODO: icon component
//                    Element(
//                        layout = {
//                            width(16f).height(16f).marginLeft(Dimensions.ContentMarginMedium)
//                                .marginTop(Dimensions.ContentMarginMedium)
//                        },
//                        styles = { background(ModSprites.ICON_GROUPING_RULE_TAG) },
//                    )
//                }
//
//                NeoListItem(
//                    title = translatable("组件物品 §6不祥之瓶"),
//                    description = translatable("item:minecraft:ominous_bottle{\"minecraft:ominous_bottle_amplifier\":2}")
//                ).addContentAt(0) {
//                    // TODO: icon component
//                    Element(
//                        layout = {
//                            width(16f).height(16f).marginLeft(Dimensions.ContentMarginMedium)
//                                .marginTop(Dimensions.ContentMarginMedium)
//                        },
//                        styles = { background(ModSprites.ICON_GROUPING_RULE_STACK) },
//                    )
//                }
//                NeoListItem(
//                    title = translatable("物品 §6药水"), description = translatable("&item:minecraft:potion")
//                ).addContentAt(0) {
//                    // TODO: icon component
//                    Element(
//                        layout = {
//                            width(16f).height(16f).marginLeft(Dimensions.ContentMarginMedium)
//                                .marginTop(Dimensions.ContentMarginMedium)
//                        },
//                        styles = { background(ModSprites.ICON_GROUPING_RULE_STACK) },
//                    )
//                }
//                NeoListItem(
//                    title = translatable("正则匹配 §6^item:.*_book$"), description = translatable("\\^item:.*_book$\\")
//                ).addContentAt(0) {
//                    // TODO: icon component
//                    Element(
//                        layout = {
//                            width(16f).height(16f).marginLeft(Dimensions.ContentMarginMedium)
//                                .marginTop(Dimensions.ContentMarginMedium)
//                        },
//                        styles = { background(ModSprites.ICON_GROUPING_RULE_REGEX) },
//                    )
//                }.apply {
//                    textContainer
//                }
//            }
        }
    }

    private fun UIScope.RuleListItem(state: StackGroupEditorViewModel.RuleUiState): UIElement {
        return Row(layout = { marginBottom(-1f) }) {

            // TODO: simplify this
            NeoListItem(
                title = state.titleComponent,
                description = state.notationComponent,
                layout = { flexFill() },
            ).addContentAt(0) {
                Element(
                    layout = { width(16f).height(16f).marginLeft(6f).marginTop(6f) },
                    styles = { background(state.icon) },
                )
            }.textContainer.apply {
                layout.heightFitContent().paddingBottom(6f)
                addContent {
                    StackPreview(
                        columns = 14,
                        paddings = StackPreview.PaddingValues(0f),
                        layout = { marginTop(2f) },
                        styles = { background(IGuiTexture.EMPTY) },
                    ).stackGroups = state.previewStacks
                }
            }

            // Delete button
            NeoClickableView(
                layout = { width(36f).gapAll(2f).alignItems(YogaAlign.CENTER).justifyItems(AlignItems.CENTER) },
                onClick = { viewModel.removeRule(state.rule) },
                actionStyle = {
                    baseTexture(OreSprites.LIST_ITEM_END).hoverTexture(OreSprites.LIST_ITEM_END_HOVERED)
                        .pressedTexture(OreSprites.LIST_ITEM_END_PRESSED)
                },
            ) {
                Element(
                    layout = { width(16f).height(16f) },
                    styles = { background(ModSprites.ICON_GROUPING_RULE_DELETE) },
                )
                Label(
                    text = translatable("Delete"),
                    textStyles = { fontSize(6f).textShadow(false).adaptiveWidth(true).adaptiveHeight(true) },
                )
            }
        }
    }

    override fun onBindData() {}

    override fun onCreated() {}

}