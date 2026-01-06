package concerrox.emixx.oreui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.CommonComponents

class Spacer(x: Int, y: Int, width: Int, height: Int) : AbstractWidget(x, y, width, height, CommonComponents.EMPTY) {

    override fun renderWidget(
        guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float
    ) {
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

}