package concerrox.emixx.content.stackgroup.editor.rule.page

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture
import concerrox.blueberry.ui.binding.map
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.LazyColumn
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.addContent
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.neo.component.NeoListItem
import concerrox.blueberry.ui.neo.component.NeoTextField
import concerrox.blueberry.ui.neo.component.NeoVerticalScrollView
import concerrox.emixx.content.stackgroup.editor.component.StackPreview
import concerrox.emixx.content.stackgroup.editor.rule.GroupingRuleDialogViewModel
import dev.vfyjxf.taffy.style.AlignItems
import net.minecraft.network.chat.Component

private fun UIScope.TagListItem(state: GroupingRuleDialogViewModel.TagUiState) = NeoListItem(
    title = state.name,
    description = Component.literal(state.key.location.toString()),
).apply {
    isCheckable = true
    layout.alignItems(AlignItems.CENTER).paddingRight(6f)
    textContainer.layout.width(128f)

    addContent {
        StackPreview(
            columns = 6,
            rows = 1,
            paddings = StackPreview.PaddingValues(0f),
            styles = { background(IGuiTexture.EMPTY) }).apply {
            stackGroups = state.content
        }
    }
}

fun UIScope.GroupingRuleTagPage(viewModel: GroupingRuleDialogViewModel) {
    Column(layout = { flexFill() }) {
        NeoTextField().apply {
            textFieldStyle.placeholder(Component.literal("Search tags…"))
        }
        NeoVerticalScrollView(layout = { flexFill() }, viewContainerLayout = { paddingTop(4f) }) {
            val tags = viewModel.tagPickerUiState.map { it.tags }
            LazyColumn(tags) {
                TagListItem(it)
            }
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