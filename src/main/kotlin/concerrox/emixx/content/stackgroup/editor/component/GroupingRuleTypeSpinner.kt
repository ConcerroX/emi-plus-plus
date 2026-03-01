package concerrox.emixx.content.stackgroup.editor.component

import concerrox.blueberry.ui.binding.LiveData
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.addContentAt
import concerrox.blueberry.ui.lowdraglib2.util.view
import concerrox.blueberry.ui.neo.component.NeoDropdownMenu
import concerrox.blueberry.ui.neo.component.NeoSpinner
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.content.stackgroup.editor.icon

fun UIScope.GroupingRuleTypeSpinner(data: LiveData<GroupingRule.Type>) = NeoSpinner(
    data,
    GroupingRule.Type.entries,
    { it.nameKey.key },
    { marginBottom(2f).marginHorizontal(-1f) },
).addContentAt(0) {
    Element(layout = { width(12f).height(12f).marginRight(4f).marginBottom(2f) }).apply {
        data.observe { style.background(it.icon.copy().setColor(0xFF1E1E1F.toInt())) }
    }
}.apply {
    val textContentProvider = NeoDropdownMenu.textItemContentProvider(dropdownMenu.textMapper)
    itemContentProvider = {
        textContentProvider(it).apply {
            addChildAt(view {
                layout.width(12f).height(12f).marginRight(4f)
                style.background(it.icon)
            }, 0)
        }
    }
}