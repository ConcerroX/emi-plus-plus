package concerrox.emixx.content.villagertrade

import com.mojang.logging.LogUtils
import concerrox.emixx.Minecraft
import concerrox.emixx.id
import concerrox.emixx.text
import concerrox.emixx.util.mutableMap
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.ListEmiIngredient
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.tags.EnchantmentTags
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerTrades
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.MapItem
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.saveddata.maps.MapItemSavedData

object VillagerTradeManager {

    internal val VILLAGER_TRADES: EmiRecipeCategory =
        EmiRecipeCategory(id("villager_trades"), EmiStack.of(Items.EMERALD))

    private val CUSTOM_VILLAGER_TRADE_TYPES =
        mutableMapOf<String, (VillagerTrades.ItemListing) -> Pair<MutableList<out EmiIngredient>, MutableList<EmiStack>>>()

    fun initialize(emiRegistry: EmiRegistry) {
        emiRegistry.addCategory(VILLAGER_TRADES)
        emiRegistry.addWorkstation(VILLAGER_TRADES, EmiStack.of(Items.VILLAGER_SPAWN_EGG))
        collectVillagerTrades(emiRegistry)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : VillagerTrades.ItemListing> addCustomVillagerTradeType(
        clazz: Class<T>, handler: (T) -> Pair<MutableList<out EmiIngredient>, MutableList<EmiStack>>
    ) {
        CUSTOM_VILLAGER_TRADE_TYPES[clazz.name] =
            handler as (VillagerTrades.ItemListing) -> Pair<MutableList<out EmiIngredient>, MutableList<EmiStack>>
    }

    private fun collectVillagerTrades(emiRegistry: EmiRegistry) {
        val level = Minecraft.level ?: return
        VillagerTrades.TRADES.forEach { (profession, levelWithTrades) ->
            levelWithTrades.forEach { (villagerLevel, trades) ->
                trades.forEach { itemListing ->
                    processRecipe(level, emiRegistry, profession, villagerLevel, itemListing)
                }
            }
        }
    }

    private fun processRecipe(
        level: ClientLevel,
        emiRegistry: EmiRegistry,
        profession: VillagerProfession,
        villagerLevel: Int,
        itemListing: VillagerTrades.ItemListing
    ) {
        val inputs: MutableList<out EmiIngredient>
        val output: MutableList<EmiStack>
        when (itemListing) {
            is VillagerTrades.EmeraldForItems -> {
                inputs = stackOf(itemListing.itemStack.itemStack)
                output = emeraldStack(itemListing.emeraldAmount)
            }

            is VillagerTrades.ItemsForEmeralds -> {
                inputs = emeraldStack(itemListing.emeraldCost)
                output = stackOf(itemListing.itemStack)
            }

            is VillagerTrades.TreasureMapForEmeralds -> {
                inputs = stackWithEmerald(itemListing.emeraldCost, ItemStack(Items.COMPASS))
                output = stackOf(MapItem.create(level, 0, 0, 2, true, true).apply {
                    MapItemSavedData.addTargetDecoration(this, BlockPos.ZERO, "+", itemListing.destinationType)
                    set(DataComponents.ITEM_NAME, Component.translatable(itemListing.displayName))
                })
            }

            is VillagerTrades.EnchantedItemForEmeralds -> {
                val itemStack =
                    itemListing.itemStack.copy().apply { set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true) }
                val possibleEnchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getTag(EnchantmentTags.ON_TRADED_EQUIPMENT).get()
                val possibleEnchantmentRanges = (5..19).flatMap { power ->
                    EnchantmentHelper.getAvailableEnchantmentResults(power, itemStack, possibleEnchantments.stream())
                }.groupBy {
                    it.enchantment.value()
                }.mapValues { (_, instances) ->
                    val levels = instances.map { it.level }
                    levels.min()..levels.max()
                }
                val possibleEnchantmentTooltips = possibleEnchantmentRanges.map { (enchantment, levelRange) ->
                    val min = levelRange.min()
                    val max = levelRange.max()
                    var text =
                        Component.literal("- ").append(enchantment.description.copy()).append(CommonComponents.SPACE)
                            .append(Component.translatable("enchantment.level.$min"))
                    if (max != min) text = text.append("-").append(Component.translatable("enchantment.level.$max"))
                    text.withStyle(ChatFormatting.GRAY)
                }

                inputs =
                    emeraldStack(itemListing.baseEmeraldCost + 5, (itemListing.baseEmeraldCost + 19).coerceAtMost(64))
                val tooltips = mutableListOf(
                    text("gui", "possible_enchantments").withStyle(ChatFormatting.AQUA),
                    *possibleEnchantmentTooltips.toTypedArray()
                ).mutableMap { ClientTooltipComponent.create(it.visualOrderText) }
                output = mutableListOf(TooltipItemEmiStack(itemStack, 1L, tooltips))
            }

            is VillagerTrades.SuspiciousStewForEmerald -> {
                inputs = emeraldStack(1)
                val components = mutableListOf<Component>()
                PotionContents.addPotionTooltip(
                    itemListing.effects.effects.map { it.createEffectInstance() },
                    { components.add(it) },
                    1F,
                    level.tickRateManager().tickrate()
                )
                output = mutableListOf(TooltipItemEmiStack(ItemStack(Items.SUSPICIOUS_STEW).apply {
                    set(DataComponents.SUSPICIOUS_STEW_EFFECTS, itemListing.effects)
                }, 1L, components.mutableMap { ClientTooltipComponent.create(it.visualOrderText) }))
            }

            is VillagerTrades.ItemsAndEmeraldsToItems -> {
                inputs = stackWithEmerald(itemListing.emeraldCost, itemListing.fromItem.itemStack)
                output = stackOf(itemListing.toItem)
            }

            is VillagerTrades.EmeraldsForVillagerTypeItem -> {
                inputs =
                    mutableListOf(ListEmiIngredient(itemListing.trades.values.map(EmiStack::of), 1), EmiStack.EMPTY)
                output = emeraldStack(1)
            }

            is VillagerTrades.EnchantBookForEmeralds -> {
                val itemStack = ItemStack(Items.ENCHANTED_BOOK)
                inputs = emeraldStack(5, 64)
                val enchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                val nonTreasureEnchantments = enchantments.getTag(EnchantmentTags.NON_TREASURE).get()
                val possibleEnchantments = enchantments.getTag(itemListing.tradeableEnchantments).get().toMutableList()
                possibleEnchantments.removeAll(nonTreasureEnchantments)
                val possibleEnchantmentRanges =
                    possibleEnchantments.associate { it.value() to it.value().minLevel..it.value().maxLevel }
                val possibleEnchantmentTooltips = possibleEnchantmentRanges.map { (enchantment, levelRange) ->
                    val min = levelRange.min()
                    val max = levelRange.max()
                    var text =
                        Component.literal("- ").append(enchantment.description.copy()).append(CommonComponents.SPACE)
                            .append(Component.translatable("enchantment.level.$min"))
                    if (max != min) text = text.append("-").append(Component.translatable("enchantment.level.$max"))
                    text.withStyle(ChatFormatting.GRAY)
                }
                val tooltips = mutableListOf(
                    text("gui", "possible_enchantments").withStyle(ChatFormatting.AQUA),
                    Component.literal("- ").append(text("gui", "possible_enchantments_for_enchanted_book"))
                        .withStyle(ChatFormatting.GRAY),
                    *possibleEnchantmentTooltips.toTypedArray()
                ).mutableMap { ClientTooltipComponent.create(it.visualOrderText) }
                output = mutableListOf(TooltipItemEmiStack(itemStack, 1L, tooltips))
            }

            is VillagerTrades.DyedArmorForEmeralds -> {
                inputs = emeraldStack(itemListing.value)
                val tooltips = mutableListOf(
                    text("gui", "random_color").withStyle(ChatFormatting.YELLOW)
                ).mutableMap { ClientTooltipComponent.create(it.visualOrderText) }
                output = mutableListOf(TooltipItemEmiStack(ItemStack(itemListing.item), 1L, tooltips))
            }

            is VillagerTrades.TippedArrowForItemsAndEmeralds -> {
                inputs =
                    stackWithEmerald(itemListing.emeraldCost, ItemStack(itemListing.fromItem, itemListing.fromCount))
                output = stackOf(ItemStack(itemListing.toItem.item, itemListing.toCount))
            }

            else -> {
                CUSTOM_VILLAGER_TRADE_TYPES.getOrElse(itemListing.javaClass.name) {
                    LogUtils.getLogger().warn("Unsupported villager trade type: $itemListing")
                    return
                }.invoke(itemListing).let {
                    inputs = it.first
                    output = it.second
                }
            }
        }
        emiRegistry.addRecipe(VillagerTradeRecipe(profession, villagerLevel, inputs, output))
    }

    private fun stackOf(itemStack: ItemStack) = mutableListOf(EmiStack.of(itemStack), EmiStack.EMPTY)
    private fun stackWithEmerald(amount: Int, itemStack2: ItemStack) =
        mutableListOf(EmiStack.of(Items.EMERALD, amount.toLong()), EmiStack.of(itemStack2))

    private fun emeraldStack(amount: Int) = mutableListOf(EmiStack.of(Items.EMERALD, amount.toLong()), EmiStack.EMPTY)
    private fun emeraldStack(min: Int, max: Int) = mutableListOf(
        AmountRangeItemEmiStack(ItemStack(Items.EMERALD), min.toLong(), max.toLong()), EmiStack.EMPTY
    )

}