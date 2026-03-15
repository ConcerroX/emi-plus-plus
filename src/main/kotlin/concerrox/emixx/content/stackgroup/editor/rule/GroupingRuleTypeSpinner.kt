package concerrox.emixx.content.stackgroup.editor.rule

import concerrox.blueberry.ui.binding.LiveData
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.neo.component.NeoSpinner
import concerrox.emixx.content.stackgroup.data.GroupingRule

fun UIScope.GroupingRuleTypeSpinner(
    data: LiveData<GroupingRule.Type>,
    onTypeChange: (GroupingRule.Type) -> Unit,
) = NeoSpinner(
    data, GroupingRule.Type.entries, { it.nameKey.key }, { marginBottom(2f).marginHorizontal(-1f) },
).apply {
    bindObserver { onTypeChange(it) }
//    addContentAt(0) {
        // Type icon
//        Element(layout = { width(12f).height(12f).marginRight(4f).marginBottom(2f) }).apply {
//            data.observe { style.background(it.icon.copy().setColor(0xFF1E1E1F.toInt())) }
//        }
//    }

//    val textContentProvider = NeoDropdownMenu.textItemContentProvider(dropdownMenu.textMapper)
//    itemContentProvider = {
//        textContentProvider(it).apply {
//            addChildAt(view {
//                layout.width(12f).height(12f).marginRight(4f)
//                style.background(it.icon)
//            }, 0)
//        }
//    }
}