package concerrox.emixx.content.stackgroup.editor.component

import com.lowdragmc.lowdraglib2.gui.ui.Style
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableUIElement
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry
import concerrox.blueberry.ui.binding.LiveData
import concerrox.blueberry.ui.binding.LiveDataSource
import concerrox.blueberry.ui.lowdraglib2.Element
import concerrox.blueberry.ui.lowdraglib2.LayoutBuilder
import concerrox.blueberry.ui.lowdraglib2.StyleBuilder
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.UIScopeImpl
import concerrox.blueberry.ui.lowdraglib2.util.toDelegate
import concerrox.blueberry.ui.neo.OreSprites
import concerrox.blueberry.util.FlattenDepth
import concerrox.blueberry.util.flattenPose
import concerrox.blueberry.util.translate
import concerrox.emixx.content.stackgroup.displaylayout.StackDisplayLayout
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.runtime.EmiDrawContext
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sign

fun UIScope.StackPreview(
    data: LiveData<out List<EmiIngredient>>? = null,
    columns: Int = StackPreview.DEFAULT_COLUMNS,
    rows: Int = StackPreview.DEFAULT_ROWS,
    paddings: StackPreview.PaddingValues = StackPreview.DEFAULT_PADDINGS,
    adaptiveHeight: Boolean = false,
    onPickup: (EmiIngredient) -> Unit = {},
    onPageChange: (Int) -> Unit = {},
    onPageCountChange: (Int) -> Unit = {},
    layout: LayoutBuilder = null,
    styles: StyleBuilder = null,
    content: (UIScopeImpl.() -> Unit)? = null,
) = Element(::StackPreview, ::UIScopeImpl, null, layout, styles, content).apply {
    this.paddings = paddings
    this.columns = columns
    this.rows = rows
    this.onPickup = onPickup
    this.onPageChange = onPageChange
    this.onPageCountChange = onPageCountChange
    stackGroupPreviewStyle.adaptiveHeight = adaptiveHeight
    data?.let { bindDataSource(LiveDataSource(it) { it }) }
}

open class StackPreview : BindableUIElement<List<EmiIngredient>>() {

    companion object {

        const val STACK_ENTRY_SIZE = 18
        const val DEFAULT_COLUMNS = 9
        const val DEFAULT_ROWS = 1
        val DEFAULT_PADDINGS = PaddingValues(all = 4f)
        val PROPERTIES = arrayOf(PropertyRegistry.ADAPTIVE_HEIGHT)

        @Deprecated("Manage this by yourself.")
        internal const val PREVIEW_STACKS_COUNT = 14

        fun calculateWidth(columns: Int, paddings: PaddingValues = DEFAULT_PADDINGS): Float {
            return columns * STACK_ENTRY_SIZE + paddings.left + paddings.right
        }

        fun calculateHeight(rows: Int, paddings: PaddingValues = DEFAULT_PADDINGS): Float {
            return rows * STACK_ENTRY_SIZE + paddings.top + paddings.bottom
        }

    }

    var paddings = DEFAULT_PADDINGS
    var stackDisplayLayout = StackDisplayLayout()
    var isHovered = false

    var stackGroups = listOf<EmiIngredient>()
        set(value) {
            field = value
            computeLayout()
            updateDisplayStackList()
        }

    var columns = DEFAULT_COLUMNS
        set(value) {
            field = value
            computeLayout()
        }

    var rows = 1
    var lastPageCount = -1
    var onPickup: (EmiIngredient) -> Unit = {}
    var onPageChange: (Int) -> Unit = {}
    var onPageCountChange: (Int) -> Unit = {}
    var displayStackList = emptyList<EmiIngredient>()
    val stackGroupPreviewStyle = StackGroupPreviewStyle()

    val pageEntries get() = columns * adaptiveRows

    var page = 0
        set(value) {
            field = value
            onPageChange(value + 1)
            updateDisplayStackList()
        }

    val pageCount: Int
        get() {
            val ret = (ceil(stackGroups.size.toFloat() / pageEntries).toInt() - 1).coerceAtLeast(0)
            if (lastPageCount != ret) {
                lastPageCount = ret
                onPageCountChange(ret + 1)
            }
            return ret
        }

    val adaptiveRows: Int
        get() {
            return if (stackGroupPreviewStyle.adaptiveHeight) {
                ceil(stackGroups.size / columns.toFloat()).toInt().coerceAtLeast(0)
            } else {
                rows
            }
        }

    private val layoutLeft get() = (positionX + paddings.left).toInt()
    private val layoutTop get() = (positionY + paddings.top).toInt()

    init {
        style.background(OreSprites.PANEL_BACKGROUND)
        addEventListener(UIEvents.MOUSE_ENTER) { isHovered = true }
        addEventListener(UIEvents.MOUSE_LEAVE) { isHovered = false }
        addEventListener(UIEvents.MOUSE_UP) { e ->
            findStackAtMouse(e.x, e.y)?.let { onPickup(it) }
        }
        addEventListener(UIEvents.MOUSE_WHEEL) {
            var newPage = page - it.deltaY.sign.toInt()
            if (newPage < 0) newPage = pageCount
            if (newPage > pageCount) newPage = 0
            page = newPage
        }
    }

    private fun findStackAtMouse(mouseX: Float, mouseY: Float): EmiIngredient? {
        if (!isHovered) return null

        val mouseTileX = (mouseX - layoutLeft) / STACK_ENTRY_SIZE
        val mouseTileY = (mouseY - layoutTop) / STACK_ENTRY_SIZE
        return displayStackList.getOrNull(mouseTileX.toInt() + mouseTileY.toInt() * columns)
    }

    private fun updateDisplayStackList() {
        displayStackList = stackGroups.subList(page * pageEntries, min((page + 1) * pageEntries, stackGroups.size))
        stackDisplayLayout.isTilesDirty = true
    }

    override fun drawBackgroundAdditional(guiContext: GUIContext) {
        super.drawBackgroundAdditional(guiContext)

        val graphics = guiContext.graphics
        val delta = guiContext.partialTick

        val mouseX = guiContext.mouseX
        val mouseY = guiContext.mouseY
        // TODO: add find tile pos
        val mouseTileX = (mouseX - layoutLeft) / STACK_ENTRY_SIZE
        val mouseTileY = (mouseY - layoutTop) / STACK_ENTRY_SIZE
        val hoverStack = findStackAtMouse(mouseX.toFloat(), mouseY.toFloat())

        if (hoverStack is EmiStack) {
            val x = layoutLeft + mouseTileX * STACK_ENTRY_SIZE
            val y = layoutTop + mouseTileY * STACK_ENTRY_SIZE
            EmiRenderHelper.drawSlotHightlight(
                EmiDrawContext.wrap(graphics), x, y, STACK_ENTRY_SIZE, STACK_ENTRY_SIZE, 0
            )
            modularUI?.setHoverTooltip(
                hoverStack.tooltipText, hoverStack.itemStack, null, null
            )
        }

        displayStackList.forEachIndexed { index, stack ->
            val tileX = index % columns
            val tileY = index / columns
            if (tileY >= adaptiveRows) return@forEachIndexed

            stackDisplayLayout.putStack(tileX, tileY, stack)

            graphics.translate(z = 5f) { // TODO: fix this
                flattenPose(FlattenDepth.ItemStack)
                val x = layoutLeft + tileX * STACK_ENTRY_SIZE + 1
                val y = layoutTop + tileY * STACK_ENTRY_SIZE + 1
                stack.render(this, x, y, delta)
            }
        }
        stackDisplayLayout.render(graphics, layoutLeft, layoutTop)
    }

    override fun getValue() = stackGroups

    override fun setValue(value: List<EmiIngredient>?, notify: Boolean): BindableUIElement<List<EmiIngredient>> {
        stackGroups = value ?: emptyList()
        return this
    }

    private fun computeLayout() {
        Style.importantPipeline(layout) {
            layout.width(calculateWidth(columns, paddings))
            layout.height(calculateHeight(adaptiveRows, paddings))
        }
        stackDisplayLayout.recreateLayout(columns, adaptiveRows)
    }

    data class PaddingValues(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        constructor(all: Float) : this(all, all, all, all)
    }

    inner class StackGroupPreviewStyle : Style(this) {
        override fun getProperties() = PROPERTIES
        var adaptiveHeight: Boolean by PropertyRegistry.ADAPTIVE_HEIGHT.toDelegate()
    }

}