package concerrox.emixx

import com.mojang.logging.LogUtils
import concerrox.emixx.content.villagertrade.VillagerTradeManager
import concerrox.emixx.mixin.BasicItemListingAccessor
import concerrox.emixx.registry.ModLang
import concerrox.emixx.registry.ModTranslationKeys
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.BasicItemListing
import net.neoforged.neoforge.data.event.GatherDataEvent
import org.jetbrains.annotations.ApiStatus

internal typealias Identifier = ResourceLocation

internal val Minecraft = net.minecraft.client.Minecraft.getInstance()
internal fun id(path: String) = Identifier.fromNamespaceAndPath(EmiPlusPlus.MOD_ID, path)
internal fun id(namespace: String, path: String) = Identifier.fromNamespaceAndPath(namespace, path)


@Deprecated("")
@ApiStatus.ScheduledForRemoval
fun text(type: String, path: String): MutableComponent = Component.translatable("$type.${EmiPlusPlus.MOD_ID}.$path")

@Deprecated("")
@ApiStatus.ScheduledForRemoval
fun text(type: String, path: String, vararg args: Any): MutableComponent =
    Component.translatable("$type.${EmiPlusPlus.MOD_ID}.$path", args)

@Deprecated("")
@ApiStatus.ScheduledForRemoval
fun text(path: String): MutableComponent = Component.translatable("${EmiPlusPlus.MOD_ID}.$path")


@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlus(private val eventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MOD_ID = "emixx"
        internal val LOGGER = LogUtils.getLogger()
    }

    init {
        initializeTranslation()
        initializeVillagerTrade()
    }

    private fun initializeTranslation() {
        ModTranslationKeys.register()
        eventBus.addListener { e: GatherDataEvent ->
//            e.generator.addProvider(e.includeClient(), ModLanguageProvider(e.generator.packOutput))
            e.generator.addProvider(e.includeClient(), ModLang.toProvider(e.generator.packOutput))
        }
    }

    private fun initializeVillagerTrade() {
        VillagerTradeManager.addCustomVillagerTradeType(BasicItemListing::class.java) { itemListing ->
            val accessor = itemListing as BasicItemListingAccessor
            val inputs = mutableListOf(EmiStack.of(accessor.price), EmiStack.of(accessor.price2))
            val output = mutableListOf(EmiStack.of(accessor.forSale))
            inputs to output
        }
    }

}