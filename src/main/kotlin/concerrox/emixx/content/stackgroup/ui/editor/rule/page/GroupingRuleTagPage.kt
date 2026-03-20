package concerrox.emixx.content.stackgroup.ui.editor.rule.page

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener
import concerrox.blueberry.ui.binding.LiveData
import concerrox.blueberry.ui.binding.map
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.LazyColumn
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.addContent
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.neo.component.NeoListItem
import concerrox.blueberry.ui.neo.component.NeoTextField
import concerrox.blueberry.ui.neo.component.NeoVerticalScrollView
import concerrox.emixx.content.stackgroup.ui.editor.component.StackPreview
import concerrox.emixx.content.stackgroup.ui.editor.rule.GroupingRuleDialogUiState
import concerrox.emixx.content.stackgroup.ui.editor.rule.PickerUiState
import concerrox.emixx.content.stackgroup.ui.editor.rule.TagUiState
import dev.vfyjxf.taffy.style.AlignItems
import net.minecraft.network.chat.Component

private fun UIScope.TagListItem(state: TagUiState, onClick: UIEventListener) = NeoListItem(
    title = state.name, description = Component.literal(state.key.location.toString()), onClick = onClick
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

fun UIScope.GroupingRuleTagPage(uiState: LiveData<GroupingRuleDialogUiState?>, onSelected: (TagUiState) -> Unit) {
    Column(layout = { flexFill() }) {
        // Search box
        NeoTextField().apply {
            textFieldStyle.placeholder(Component.literal("Search tags…"))
        }

        // Tag picker
        NeoVerticalScrollView(layout = { flexFill() }, viewContainerLayout = { paddingTop(4f) }) {
            LazyColumn(uiState.map { (it?.pickerUiState as PickerUiState.Tag).tags }) { item ->
                TagListItem(item, onClick = { e ->
                    parent.children.forEach { child ->
                        if (child is NeoListItem && child != e.currentElement) child.isChecked = false
                        onSelected(item)
                    }
                })
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