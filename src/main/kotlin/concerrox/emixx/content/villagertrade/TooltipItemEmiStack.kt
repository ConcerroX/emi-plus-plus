package concerrox.emixx.content.villagertrade

import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.ItemEmiStack
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

@Suppress("UnstableApiUsage")
class TooltipItemEmiStack(
    private val stack: ItemStack, amount: Long, private val tooltips: MutableList<ClientTooltipComponent>
) : ItemEmiStack(stack, amount) {

    override fun copy() = TooltipItemEmiStack(stack, amount, tooltips)

    override fun getTooltip(): MutableList<ClientTooltipComponent> {
        val result = super.getTooltip()
        result.addAll(tooltips)
        return result
    }

    override fun getEmiStacks(): MutableList<EmiStack> {
        return mutableListOf(EmiStack.of(Items.APPLE), EmiStack.of(Items.STRING), EmiStack.of(Items.DIORITE))
    }

}