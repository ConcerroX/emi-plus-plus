package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.world.item.MinecartItem

internal class MinecartItemGroup: StackGroup("minecarts", Type.ITEM) {

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false
        return stack.itemStack.item is MinecartItem
    }

}