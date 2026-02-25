package concerrox.emixx.content.stackgroup.editor

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
import concerrox.blueberry.util.FlattenDepth
import concerrox.blueberry.util.flattenPose
import concerrox.blueberry.util.translate
import concerrox.emixx.content.stackgroup.displaylayout.StackDisplayLayout
import concerrox.emixx.registry.ModSprites
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.runtime.EmiDrawContext
import dev.vfyjxf.taffy.style.TaffyDimension
import kotlin.math.ceil

fun UIScope.StackPreview(
    data: LiveData<List<EmiStack>>? = null,
    columns: Int = StackPreview.DEFAULT_COLUMNS,
    rows: Int = StackPreview.DEFAULT_ROWS,
    paddings: StackPreview.PaddingValues = StackPreview.DEFAULT_PADDINGS,
    adaptiveHeight: Boolean = false,
    layout: LayoutBuilder = null,
    styles: StyleBuilder = null,
    content: (UIScopeImpl.() -> Unit)? = null,
) = Element(::StackPreview, ::UIScopeImpl, null, layout, styles, content).apply {
    this.paddings = paddings
    this.columns = columns
    this.rows = rows
    stackGroupPreviewStyle.adaptiveHeight = adaptiveHeight
    data?.let { bindDataSource(LiveDataSource(it)) }
}

open class StackPreview : BindableUIElement<List<EmiStack>>() {

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

    var stackGroups = listOf<EmiStack>()
        set(value) {
            field = value
            computeLayout()
        }

    var columns = DEFAULT_COLUMNS
        set(value) {
            field = value
            computeLayout()
        }

    var rows = 1
    val stackGroupPreviewStyle = StackGroupPreviewStyle()

    val adaptiveRows: Int
        get() {
            return if (stackGroupPreviewStyle.adaptiveHeight) {
                ceil(stackGroups.size / columns.toFloat()).toInt().coerceAtLeast(1)
            } else {
                rows
            }
        }

    private val layoutLeft get() = (positionX + paddings.left).toInt()
    private val layoutTop get() = (positionY + paddings.top).toInt()
    private val layoutWidth get() = (paddingWidth - paddings.left - paddings.right).toInt()
    private val layoutHeight get() = (paddingHeight - paddings.top - paddings.bottom).toInt()

    init {
        style.background(ModSprites.STACK_GROUP_PREVIEW_BACKGROUND)
        addEventListener(UIEvents.MOUSE_ENTER) { isHovered = true }
        addEventListener(UIEvents.MOUSE_LEAVE) { isHovered = false }
    }

    override fun drawBackgroundAdditional(guiContext: GUIContext) {
        super.drawBackgroundAdditional(guiContext)

        val graphics = guiContext.graphics
        val delta = guiContext.partialTick

        val mouseX = guiContext.mouseX
        val mouseY = guiContext.mouseY
        val isMouseInLayout = isMouseOver(
            layoutLeft.toFloat(), layoutTop.toFloat(),
            layoutWidth.toFloat(), layoutHeight.toFloat(),
            mouseX.toFloat(), mouseY.toFloat(),
        )
        if (isHovered && isMouseInLayout) {
            val mouseTileX = (mouseX - layoutLeft) / STACK_ENTRY_SIZE
            val mouseTileY = (mouseY - layoutTop) / STACK_ENTRY_SIZE
            val hoverStack = stackGroups.getOrNull(mouseTileX + mouseTileY * columns)

            if (hoverStack != null) {
                val x = layoutLeft + mouseTileX * STACK_ENTRY_SIZE
                val y = layoutTop + mouseTileY * STACK_ENTRY_SIZE
                EmiRenderHelper.drawSlotHightlight(
                    EmiDrawContext.wrap(graphics), x, y, STACK_ENTRY_SIZE, STACK_ENTRY_SIZE, 0
                )
                modularUI?.setHoverTooltip(hoverStack.tooltipText, hoverStack.itemStack, null, null)
            } else {
                modularUI?.cleanTooltip()
            }
        }

        stackGroups.forEachIndexed { index, stack ->
            val tileX = index % columns
            val tileY = index / columns
            if (tileY >= adaptiveRows) return@forEachIndexed

            stackDisplayLayout.putStack(tileX, tileY, stack)

            graphics.translate(z = 5f) {
                flattenPose(FlattenDepth.ItemStack)
                val x = layoutLeft + tileX * STACK_ENTRY_SIZE + 1
                val y = layoutTop + tileY * STACK_ENTRY_SIZE + 1
                stack.render(this, x, y, delta)
            }
        }
        stackDisplayLayout.render(graphics, layoutLeft, layoutTop)
    }

    override fun getValue() = stackGroups

    override fun setValue(value: List<EmiStack>?, notify: Boolean): BindableUIElement<List<EmiStack>> {
        stackGroups = value ?: emptyList()
        return this
    }

    private fun computeLayout() {
        Style.importantPipeline(layout) {
            layout.width(calculateWidth(columns, paddings))
            if (layout.height == TaffyDimension.auto()) layout.height(calculateHeight(adaptiveRows, paddings))
        }
        stackDisplayLayout.recreateLayout(columns, adaptiveRows)
    }

    data class PaddingValues(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        constructor(all: Float) : this(all, all, all, all)
        constructor(horizontal: Float, vertical: Float) : this(horizontal, vertical, horizontal, vertical)
    }

    inner class StackGroupPreviewStyle : Style(this) {
        override fun getProperties() = PROPERTIES
        var adaptiveHeight: Boolean by PropertyRegistry.ADAPTIVE_HEIGHT.toDelegate()
    }

}