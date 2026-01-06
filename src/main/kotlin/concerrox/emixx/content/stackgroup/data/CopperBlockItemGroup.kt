package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.WeatheringCopperBulbBlock
import net.minecraft.world.level.block.WeatheringCopperFullBlock
import net.minecraft.world.level.block.WeatheringCopperGrateBlock

internal class CopperBlockItemGroup : StackGroup(ResourceLocation.withDefaultNamespace("copper_blocks")) {

    companion object {
        private val WAXED_COPPER_BLOCK_ITEMS = arrayOf(
            Items.WAXED_COPPER_BLOCK,
            Items.WAXED_CHISELED_COPPER,
            Items.WAXED_COPPER_GRATE,
            Items.WAXED_CUT_COPPER,
            Items.WAXED_COPPER_BULB,

            Items.WAXED_EXPOSED_COPPER,
            Items.WAXED_EXPOSED_CHISELED_COPPER,
            Items.WAXED_EXPOSED_COPPER_GRATE,
            Items.WAXED_EXPOSED_CUT_COPPER,
            Items.WAXED_EXPOSED_COPPER_BULB,

            Items.WAXED_WEATHERED_COPPER,
            Items.WAXED_WEATHERED_CHISELED_COPPER,
            Items.WAXED_WEATHERED_COPPER_GRATE,
            Items.WAXED_WEATHERED_CUT_COPPER,
            Items.WAXED_WEATHERED_COPPER_BULB,

            Items.WAXED_OXIDIZED_COPPER,
            Items.WAXED_OXIDIZED_CHISELED_COPPER,
            Items.WAXED_OXIDIZED_COPPER_GRATE,
            Items.WAXED_OXIDIZED_CUT_COPPER,
            Items.WAXED_OXIDIZED_COPPER_BULB
        )
    }

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false
        val item = stack.itemStack.item
        return item is BlockItem && (item.block is WeatheringCopperFullBlock || item.block is WeatheringCopperGrateBlock || item.block is WeatheringCopperBulbBlock ||
                item in WAXED_COPPER_BLOCK_ITEMS)
    }

}