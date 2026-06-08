package concerrox.emixx.content.stackgroup.data

import concerrox.emixx.Identifier
import concerrox.emixx.content.stackgroup.data.group.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.registry.RegistryTokens
import concerrox.emixx.content.stackgroup.data.rule.GroupingRule
import concerrox.emixx.id
import concerrox.emixx.minecraftId
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.neoforged.neoforge.common.Tags

object BuiltInStackGroupPresets {

    val PRESETS = arrayOf(
        minecraftStackGroup("logs", "tag.item.minecraft.logs", itemTag(ItemTags.LOGS)),
        minecraftStackGroup("planks", "tag.item.minecraft.planks", itemTag(ItemTags.PLANKS)),
        minecraftStackGroup("wooden_stairs", "tag.item.minecraft.wooden_stairs", itemTag(ItemTags.WOODEN_STAIRS)),
        minecraftStackGroup("wooden_slabs", "tag.item.minecraft.wooden_slabs", itemTag(ItemTags.WOODEN_SLABS)),
        minecraftStackGroup("wooden_doors", "tag.item.minecraft.wooden_doors", itemTag(ItemTags.WOODEN_DOORS)),
        minecraftStackGroup(
            "wooden_trapdoors", "tag.item.minecraft.wooden_trapdoors", itemTag(ItemTags.WOODEN_TRAPDOORS)
        ),
        minecraftStackGroup(
            "wooden_pressure_plates",
            "tag.item.minecraft.wooden_pressure_plates",
            itemTag(ItemTags.WOODEN_PRESSURE_PLATES)
        ),
        minecraftStackGroup("wooden_buttons", "tag.item.minecraft.wooden_buttons", itemTag(ItemTags.WOODEN_BUTTONS)),
        minecraftStackGroup(
            "fences_and_gates",
            "stackGroup.minecraft.fences_and_gates",
            itemTag(ItemTags.FENCES),
            itemTag(ItemTags.FENCE_GATES)
        ),
        minecraftStackGroup("walls", "tag.item.minecraft.walls", itemTag(ItemTags.WALLS)),
        minecraftStackGroup(
            "stones",
            "tag.item.c.stones",
            itemTag(Tags.Items.STONES),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("prismarine")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("netherrack")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("basalt")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("blackstone")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("end_stone")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("calcite")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("dripstone_block")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("obsidian")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("crying_obsidian")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("bedrock")),
        ),
        minecraftStackGroup(
            "stone_building_blocks", "stackGroup.minecraft.stone_building_blocks",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("smooth_stone")),
            itemTag(ItemTags.STONE_BRICKS),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_granite")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_andesite")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_diorite")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("chiseled_deepslate")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_deepslate")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("deepslate_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("cracked_deepslate_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("deepslate_tiles")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("cracked_deepslate_tiles")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("reinforced_deepslate")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("chiseled_tuff")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_tuff")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("tuff_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("chiseled_tuff_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("prismarine_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("dark_prismarine")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("smooth_basalt")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_basalt")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_blackstone")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("chiseled_polished_blackstone")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("polished_blackstone_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("cracked_polished_blackstone_bricks")),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("end_stone_bricks")),
        ),
        minecraftStackGroup("cobblestones", "tag.item.minecraft.cobblestones", itemTag(Tags.Items.COBBLESTONES)),
        minecraftStackGroup("stairs", "tag.item.minecraft.stairs", itemTag(ItemTags.STAIRS)),
        minecraftStackGroup("slabs", "tag.item.minecraft.slabs", itemTag(ItemTags.SLABS)),
        minecraftStackGroup("sandstones", "tag.item.c.sandstone_blocks", itemTag(Tags.Items.SANDSTONE_BLOCKS)),
        minecraftStackGroup(
            "copper_blocks",
            "stackGroup.minecraft.copper_blocks",
            GroupingRule.Regex(RegistryTokens.BLOCK, "copper".toRegex())
        ),
        minecraftStackGroup(
            "quartz_blocks",
            "stackGroup.minecraft.quartz_blocks",
            GroupingRule.Regex(RegistryTokens.BLOCK, "quartz".toRegex())
        ),
        minecraftStackGroup("storage_blocks", "tag.item.c.storage_blocks", itemTag(Tags.Items.STORAGE_BLOCKS)),
        minecraftStackGroup("ingots", "tag.item.c.ingots", itemTag(Tags.Items.INGOTS)),
        minecraftStackGroup("wool", "tag.item.minecraft.wool", itemTag(ItemTags.WOOL)),
        minecraftStackGroup("wool_carpets", "tag.item.minecraft.wool_carpets", itemTag(ItemTags.WOOL_CARPETS)),
        minecraftStackGroup("terracottas", "stackGroup.item.minecraft.terracottas", itemTag(ItemTags.TERRACOTTA)),
        minecraftStackGroup(
            "glazed_terracottas", "tag.item.c.glazed_terracottas", itemTag(Tags.Items.GLAZED_TERRACOTTAS)
        ),
        minecraftStackGroup("glass_blocks", "tag.item.c.glass_blocks", itemTag(Tags.Items.GLASS_BLOCKS)),
        minecraftStackGroup("glass_panes", "tag.item.c.glass_panes", itemTag(Tags.Items.GLASS_PANES)),
        minecraftStackGroup("shulker_boxes", "tag.item.c.shulker_boxes", itemTag(Tags.Items.SHULKER_BOXES)),
        minecraftStackGroup("beds", "tag.item.minecraft.beds", itemTag(ItemTags.BEDS)),
        minecraftStackGroup("candles", "tag.item.minecraft.candles", itemTag(ItemTags.CANDLES)),
        minecraftStackGroup("banners", "tag.item.minecraft.banners", itemTag(ItemTags.BANNERS)),
        minecraftStackGroup(
            "ores",
            "tag.item.c.ores",
            itemTag(Tags.Items.ORES),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("gilded_blackstone")),
        ),
        minecraftStackGroup("leaves", "tag.item.minecraft.leaves", itemTag(ItemTags.LEAVES)),
        minecraftStackGroup("saplings", "tag.item.minecraft.saplings", itemTag(ItemTags.SAPLINGS)),
        minecraftStackGroup("flowers", "tag.item.minecraft.flowers", itemTag(ItemTags.FLOWERS)),
        minecraftStackGroup("seeds", "tag.item.c.seeds", itemTag(Tags.Items.SEEDS)),
        minecraftStackGroup(
            "corals", "stackGroup.minecraft.corals", GroupingRule.Regex(RegistryTokens.BLOCK, "coral".toRegex())
        ),
        minecraftStackGroup(
            "paintings",
            "stackGroup.minecraft.paintings",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("painting"))
        ),
        minecraftStackGroup(
            "signs", "tag.item.minecraft.signs", itemTag(ItemTags.SIGNS), itemTag(ItemTags.HANGING_SIGNS)
        ),
        minecraftStackGroup(
            "boats", "tag.item.minecraft.boats", itemTag(ItemTags.BOATS), itemTag(ItemTags.CHEST_BOATS)
        ),
        minecraftStackGroup("shovels", "tag.item.minecraft.shovels", itemTag(ItemTags.SHOVELS)),
        minecraftStackGroup("pickaxes", "tag.item.minecraft.pickaxes", itemTag(ItemTags.PICKAXES)),
        minecraftStackGroup("axes", "tag.item.minecraft.axes", itemTag(ItemTags.AXES)),
        minecraftStackGroup("hoes", "tag.item.minecraft.hoes", itemTag(ItemTags.HOES)),
        minecraftStackGroup("swords", "tag.item.minecraft.swords", itemTag(ItemTags.SWORDS)),
        minecraftStackGroup("head_armor", "tag.item.minecraft.head_armor", itemTag(ItemTags.HEAD_ARMOR)),
        minecraftStackGroup("chest_armor", "tag.item.minecraft.chest_armor", itemTag(ItemTags.CHEST_ARMOR)),
        minecraftStackGroup("leg_armor", "tag.item.minecraft.leg_armor", itemTag(ItemTags.LEG_ARMOR)),
        minecraftStackGroup("foot_armor", "tag.item.minecraft.foot_armor", itemTag(ItemTags.FOOT_ARMOR)),
        minecraftStackGroup(
            "animal_armor",
            "stackGroup.minecraft.animal_armor",
            GroupingRule.Regex(RegistryTokens.ITEM, "horse_armor".toRegex()),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("wolf_armor"))
        ),
        minecraftStackGroup(
            "infested_stones",
            "stackGroup.minecraft.infested_stones",
            GroupingRule.Regex(RegistryTokens.ITEM, "infested_".toRegex())
        ),
        minecraftStackGroup(
            "arrows",
            "tag.item.minecraft.arrows",
            itemTag(ItemTags.ARROWS),
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("tipped_arrow"))
        ),
        minecraftStackGroup("buckets", "tag.item.c.buckets", itemTag(Tags.Items.BUCKETS)),
        minecraftStackGroup(
            "goat_horns",
            "stackGroup.minecraft.goat_horns",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("goat_horn"))
        ),
        minecraftStackGroup("music_discs", "tag.item.c.music_discs", itemTag(Tags.Items.MUSIC_DISCS)),
        minecraftStackGroup("foods", "tag.item.c.foods", itemTag(Tags.Items.FOODS)),
        minecraftStackGroup("skulls", "tag.item.minecraft.skulls", itemTag(ItemTags.SKULLS)),
        minecraftStackGroup(
            "potions",
            "stackGroup.minecraft.potions",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("potion"))
        ),
        minecraftStackGroup(
            "splash_potions",
            "stackGroup.minecraft.splash_potions",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("splash_potion"))
        ),
        minecraftStackGroup(
            "lingering_potions",
            "stackGroup.minecraft.lingering_potions",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("lingering_potion"))
        ),
        minecraftStackGroup("dyes", "tag.item.c.dyes", itemTag(Tags.Items.DYES)),
        minecraftStackGroup(
            "banner_patterns",
            "stackGroup.minecraft.banner_patterns",
            GroupingRule.Regex(RegistryTokens.ITEM, "banner_pattern".toRegex())
        ),
        minecraftStackGroup(
            "decorated_pot_sherds", "tag.item.minecraft.decorated_pot_sherds", itemTag(ItemTags.DECORATED_POT_SHERDS)
        ),
        minecraftStackGroup("trim_templates", "tag.item.minecraft.trim_templates", itemTag(ItemTags.TRIM_TEMPLATES)),
        minecraftStackGroup(
            "enchanted_books",
            "stackGroup.minecraft.enchanted_books",
            GroupingRule.Identifier(RegistryTokens.ITEM, minecraftId("enchanted_book"))
        ),
        minecraftStackGroup(
            "spawn_eggs",
            "stackGroup.minecraft.spawn_eggs",
            GroupingRule.Regex(RegistryTokens.ITEM, "spawn_egg".toRegex())
        ),
        minecraftStackGroup("concretes", "tag.item.c.concretes", itemTag(Tags.Items.CONCRETES)),
        minecraftStackGroup("concrete_powders", "tag.item.c.concrete_powders", itemTag(Tags.Items.CONCRETE_POWDERS)),
    )

    private fun stackGroup(
        namespace: String, path: String, nameKey: String, vararg includes: GroupingRule
    ) = EmiStackGroupV2(
        id(namespace, "builtin/$path"), nameKey, true, includes.toList()
    )

    private fun minecraftStackGroup(path: String, nameKey: String, vararg includes: GroupingRule) = stackGroup(
        Identifier.DEFAULT_NAMESPACE, path, nameKey, *includes
    )

    private fun itemTag(tagKey: TagKey<Item>) = GroupingRule.Tag(RegistryTokens.ITEM, tagKey)

}