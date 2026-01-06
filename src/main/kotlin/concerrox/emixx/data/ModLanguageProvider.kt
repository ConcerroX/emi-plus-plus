package concerrox.mochests.data

import concerrox.mochests.MoChests
import concerrox.mochests.content.chest.MoChestBlock
import concerrox.mochests.registry.ModBlocks
import concerrox.mochests.registry.ModTranslationKeys
import net.minecraft.data.PackOutput
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.registries.DeferredHolder

class ModLanguageProvider(output: PackOutput) : LanguageProvider(output, MoChests.MOD_ID, "en_us") {

    override fun addTranslations() {
        ModTranslationKeys.TRANSLATION_KEYS.forEach {
            add(it.key, it.defaultValue)
        }
        addChest(ModBlocks.IRON_CHEST)
        addChest(ModBlocks.GOLD_CHEST)
        addChest(ModBlocks.DIAMOND_CHEST)
        addChest(ModBlocks.NETHERITE_CHEST)
        addChest(ModBlocks.GLASS_CHEST)
    }

    private fun addChest(holder: DeferredHolder<Block, MoChestBlock>) {
        addBlock(holder, holder.get().chestMaterial.translationKey.defaultValue)
    }

}