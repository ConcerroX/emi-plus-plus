package concerrox.emixx.oreui.component

import concerrox.emixx.mixin.ScreenAccessor
import concerrox.emixx.oreui.view.AutoInitializable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class ToggleButtonGroup(items: Iterable<Item>, height: Int = TextButton.SIZE_NORMAL) :
    AbstractContainerWidget(0, 0, 0, height, null), Layout, AutoInitializable {

    private val buttons = items.map { item ->
        TextButton(
            message = item.label,
            height = height,
            buttonStyle = TextButton.Style.TAB,
            onClickedListener = ::onButtonClicked
        )
    }
    private val layout = LinearLayout.horizontal().spacing(-1)
    private val itemCount: Int
        get() = buttons.size
    var onButtonCheckedChangeListener: (TextButton) -> Unit = {}

    init {
        buttons.forEach(layout::addChild)
    }

    override fun onInitialized(screen: Screen) {
        buttons.forEach { (screen as ScreenAccessor).addRenderableWidgetExternal(it) }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
    override fun children() = buttons

    @Deprecated("")
    override fun visitChildren(visitor: Consumer<LayoutElement>) {
        buttons.forEach(visitor)
    }

    @Deprecated("")
    override fun visitWidgets(visitor: Consumer<AbstractWidget>) {
        buttons.forEach(visitor)
    }

    override fun arrangeElements() {
        buttons.forEach {
            it.width = (width + itemCount - 1) / itemCount
        }
        if (buttons.sumOf { it.width } + (buttons.size - 1) != width) buttons.last().width += 1
        layout.apply {
            setPosition(this@ToggleButtonGroup.x, this@ToggleButtonGroup.y)
            arrangeElements()
        }
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {

    }

    private fun onButtonClicked(button: Button) {
        buttons.forEach { it.isChecked = false }
        button.isChecked = true
        onButtonCheckedChangeListener(button as TextButton)
    }

    data class Item(var label: Component)

}