package concerrox.emixx.content.stackgroup.editor

import concerrox.blueberry.ui.binding.LiveData
import concerrox.blueberry.ui.binding.LiveDataSource
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.LayoutBuilder
import concerrox.blueberry.ui.lowdraglib2.StyleBuilder
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.UIScopeImpl
import dev.emi.emi.api.stack.EmiStack

fun UIScope.StackPicker(
    data: LiveData<List<EmiStack>>? = null,
    columns: Int = StackPreview.DEFAULT_COLUMNS,
    rows: Int = StackPreview.DEFAULT_ROWS,
    paddings: StackPreview.PaddingValues = StackPicker.DEFAULT_PADDINGS,
    adaptiveHeight: Boolean = false,
    layout: LayoutBuilder = null,
    styles: StyleBuilder = null,
    content: (UIScopeImpl.() -> Unit)? = null,
) = Element(::StackPicker, ::UIScopeImpl, null, layout, styles, content).apply {
    this.paddings = paddings
    this.columns = columns
    this.rows = rows
    stackGroupPreviewStyle.adaptiveHeight = adaptiveHeight
    data?.let { bindDataSource(LiveDataSource(it)) }
}

class StackPicker : StackPreview() {

    companion object {
        val DEFAULT_PADDINGS = PaddingValues(4f, 32f, 4f, 4f)
    }

    init {
        paddings = DEFAULT_PADDINGS
        layout.paddingAll(4f)
    }

}