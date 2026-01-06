package concerrox.emixx.oreui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

abstract class Button(
    message: Component,
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    var isEnabled: Boolean = true,
    var isCheckable: Boolean = false,
    var isChecked: Boolean = false,
    var onClickedListener: OnClickedListener,
) : net.minecraft.client.gui.components.Button(
    x, y, width, height, message, null, DEFAULT_NARRATION
) {

    internal var isPressed = false
    internal val isPressedOrChecked
        get() = isPressed || isChecked

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        throw NotImplementedError("ClickableWidget.renderWidget must be implemented. ")
    }

    override fun onPress() {
        isPressed = true
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        isPressed = false
        if (rectangle.containsPoint(mouseX.toInt(), mouseY.toInt())) onClickedListener.onClicked(this)
    }

    fun interface OnClickedListener {
        fun onClicked(widget: Button)
    }

}