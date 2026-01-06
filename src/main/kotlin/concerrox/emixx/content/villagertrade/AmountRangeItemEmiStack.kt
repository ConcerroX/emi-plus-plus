package concerrox.emixx.content.villagertrade

import com.mojang.blaze3d.platform.Lighting
import concerrox.emixx.Minecraft
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.render.EmiRender
import dev.emi.emi.api.stack.ItemEmiStack
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import kotlin.math.min

@Suppress("UnstableApiUsage")
class AmountRangeItemEmiStack(private val stack: ItemStack, private val min: Long, private val max: Long) :
    ItemEmiStack(stack, max) {

    override fun copy() = AmountRangeItemEmiStack(stack, min, max)

    override fun render(draw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) {
        val context = EmiDrawContext.wrap(draw)
        val stack = itemStack
        if ((flags and RENDER_ICON) != 0) {
            Lighting.setupFor3DItems()
            draw.renderFakeItem(stack, x, y)
            draw.renderItemDecorations(Minecraft.font, stack, x, y, "")
        }
        if ((flags and RENDER_AMOUNT) != 0) {
            context.push()
            context.matrices().translate(0f, 0f, 200f)
            val tx = x + 17 - min(18, EmiRenderHelper.CLIENT.font.width(EmiPort.literal("$min-$max")))
            context.drawTextWithShadow(EmiPort.literal("$min-$max"), tx, y + 9, -1)
            context.pop()
        }
        if ((flags and RENDER_REMAINDER) != 0) {
            EmiRender.renderRemainderIcon(this, context.raw(), x, y)
        }
    }

}