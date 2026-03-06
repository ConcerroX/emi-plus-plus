package concerrox.emixx.content.stackgroup.editor.rule.page

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.addContent
import concerrox.blueberry.ui.neo.component.NeoListItem
import concerrox.blueberry.ui.neo.component.NeoTextField
import concerrox.blueberry.ui.neo.component.NeoVerticalScrollView
import concerrox.emixx.content.stackgroup.editor.StackGroupEditorViewModel
import concerrox.emixx.content.stackgroup.editor.component.StackPreview
import concerrox.emixx.content.stackgroup.editor.rule.GroupingRuleDialogViewModel
import dev.emi.emi.registry.EmiStackList
import dev.vfyjxf.taffy.style.AlignItems
import net.minecraft.network.chat.Component

private fun UIScope.TagListItem(state: StackGroupEditorViewModel.RuleUiState? = null) = NeoListItem(
    title = Component.literal("Dyes"),
    description = Component.literal("c:dyes"),
//        title = state.titleComponent,
//        description = state.notationComponent,
//        layout = { flexFill() },
).apply {
    layout.alignItems(AlignItems.CENTER).paddingRight(6f)
    textContainer.layout.width(64f)
    addContent {
        StackPreview(
            columns = 6,
            rows = 1,
            paddings = StackPreview.PaddingValues(0f),
            styles = { background(IGuiTexture.EMPTY) }
        ).apply {
            stackGroups = EmiStackList.filteredStacks.subList(0, 20)
        }
    }
}

fun UIScope.GroupingRuleTagPage(viewModel: GroupingRuleDialogViewModel) {
    Column {
        NeoTextField().apply {
            textFieldStyle.placeholder(Component.literal("Search tags…"))
        }
        NeoVerticalScrollView(viewContainerLayout = { paddingTop(4f) }) {
            TagListItem()
        }
    }

//    NeoPreference(layout = { paddingHorizontal(8f) }) {
//        val stacks = liveData { EmiStackList.filteredStacks }
//
//        StackPreview(
//            data = stacks,
//            rows = 12,
//        )
//    }.apply {
//        titleElement.setDisplay(false)
//    }
}