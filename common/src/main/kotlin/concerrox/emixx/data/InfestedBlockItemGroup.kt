package concerrox.emixx.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.InfestedBlock

internal class InfestedBlockItemGroup : StackGroup("infested_blocks", StackGroup.Type.ITEM) {

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false
        val item = stack.itemStack.item
        return item is BlockItem && item.block is InfestedBlock
    }

}