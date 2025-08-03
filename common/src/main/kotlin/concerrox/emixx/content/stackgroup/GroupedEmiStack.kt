package concerrox.emixx.content.stackgroup

import concerrox.emixx.content.stackgroup.data.StackGroup
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.util.function.Function

class GroupedEmiStack<T : EmiStack>(val realStack: T, val stackGroup: StackGroup) : EmiStack() {

    override fun render(draw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) =
        realStack.render(draw, x, y, delta, flags)
    override fun comparison(comparison: Comparison?): EmiStack = realStack.comparison(comparison)
    override fun comparison(comparison: Function<Comparison, Comparison>?): EmiStack = realStack.comparison(comparison)
    override fun setRemainder(stack: EmiStack?): EmiStack = realStack.setRemainder(stack)
    override fun getEmiStacks(): MutableList<EmiStack> = realStack.emiStacks
    override fun getRemainder(): EmiStack = realStack.remainder
    override fun isEmpty() = realStack.isEmpty
    override fun copy(): EmiStack = realStack.copy()
    override fun getComponentChanges(): DataComponentPatch = realStack.componentChanges
    override fun getKey(): Any = realStack.key
    override fun getId(): ResourceLocation = realStack.id
    override fun getTooltipText(): MutableList<Component> = realStack.tooltipText
    override fun getName(): Component = realStack.name
    override fun getAmount(): Long = realStack.amount
    override fun setAmount(amount: Long): EmiStack = realStack.setAmount(amount)
    override fun getChance(): Float = realStack.chance
    override fun setChance(chance: Float): EmiStack = realStack.setChance(chance)
    override fun <T : Any?> get(type: DataComponentType<out T>?): T? = realStack.get(type)
    override fun <T : Any?> getOrDefault(type: DataComponentType<out T>?, fallback: T): T =
        realStack.getOrDefault(type, fallback)
    override fun <T : Any?> getKeyOfType(clazz: Class<T>?): T? = realStack.getKeyOfType(clazz)
    override fun getItemStack(): ItemStack = realStack.itemStack
    override fun isEqual(stack: EmiStack?): Boolean = realStack.isEqual(stack)
    override fun isEqual(stack: EmiStack?, comparison: Comparison?): Boolean = realStack.isEqual(stack, comparison)
    override fun getTooltip(): MutableList<ClientTooltipComponent> = realStack.getTooltip()
    override fun equals(other: Any?): Boolean = realStack == other
    override fun hashCode(): Int = realStack.hashCode()
    override fun toString(): String = realStack.toString()

}
