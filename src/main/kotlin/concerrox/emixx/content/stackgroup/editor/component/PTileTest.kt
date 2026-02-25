package concerrox.emixx.content.stackgroup.editor.component

import concerrox.emixx.content.stackgroup.displaylayout.StackDisplayLayout
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.gui.GuiGraphics

class PTileTest(tileX: Int, tileY: Int) : StackDisplayLayout.Tile(tileX, tileY) {

    override fun render(guiGraphics: GuiGraphics, x: Int, y: Int) {
        EmiRenderHelper.drawSlotHightlight(EmiDrawContext.wrap(guiGraphics), x, y, 18, 18, 0)
    }

}