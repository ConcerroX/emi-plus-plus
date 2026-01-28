package concerrox.emixx.config

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.content.EmiScreenAttachmentManager
import concerrox.emixx.content.headerfooter.HeaderFooterWidgetType
import concerrox.emixx.util.boolean
import concerrox.emixx.util.enum
import concerrox.emixx.util.group
import concerrox.emixx.util.integer
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.io.path.div

class EmiPlusPlusConfig(builder: ModConfigSpec.Builder) {

    companion object {
        private val CONFIG_PAIR = ModConfigSpec.Builder().configure(::EmiPlusPlusConfig)
        val CONFIG_SPEC: ModConfigSpec = CONFIG_PAIR.right
        val CONFIG_DIRECTORY_PATH = FMLPaths.CONFIGDIR.get() / EmiPlusPlus.MOD_ID

        fun save() {
            CONFIG_SPEC.save()
        }

        lateinit var indexSidebarHeaderWidget: ModConfigSpec.EnumValue<HeaderFooterWidgetType>
        lateinit var indexSidebarFooterWidget: ModConfigSpec.EnumValue<HeaderFooterWidgetType>
        lateinit var indexSidebarTabButtonSize: ModConfigSpec.IntValue
        lateinit var hidePaginationButtons: ModConfigSpec.BooleanValue

        lateinit var isf: ModConfigSpec.BooleanValue

        lateinit var enableCreativeModeTabs: ModConfigSpec.BooleanValue
        lateinit var syncSelectedCreativeModeTab: ModConfigSpec.BooleanValue
        lateinit var disabledCreativeModeTabs: ModConfigSpec.ConfigValue<List<String>>
        lateinit var enableStackGroups: ModConfigSpec.BooleanValue
        lateinit var disabledStackGroups: ModConfigSpec.ConfigValue<List<String>>
    }

    init {
        builder.group("ui") {
            enum(::indexSidebarHeaderWidget, HeaderFooterWidgetType.CREATIVE_MODE_TABS)
            enum(::indexSidebarFooterWidget, HeaderFooterWidgetType.CUSTOM_TABS)
            integer(::indexSidebarTabButtonSize, EmiScreenAttachmentManager.EMI_TILE_ENTRY_SIZE, 1..64)
            boolean(::hidePaginationButtons, false)
        }
        builder.group("stackGroups") {
            boolean(::isf, false)
        }
        builder.group("creativeModeTabs") {
            enableCreativeModeTabs = define("enableCreativeModeTabs", true)
            syncSelectedCreativeModeTab = define("syncSelectedCreativeModeTab", true)
            disabledCreativeModeTabs = defineListAllowEmpty("disabledCreativeModeTabs", {
                listOf("minecraft:op_blocks")
            }, { "" }, { it is String })
        }
//        builder.group("stackGroups") {
//            enableStackGroups = define("enableStackGroups", true)
//            disabledStackGroups = defineListAllowEmpty("disabledStackGroups", {
//                listOf(
//                    "minecraft:shovels",
//                    "minecraft:pickaxes",
//                    "minecraft:axes",
//                    "minecraft:head_armor",
//                    "minecraft:hoes",
//                    "minecraft:swords",
//                    "minecraft:chest_armor",
//                    "minecraft:leg_armor",
//                    "minecraft:foot_armor",
//                    "c:raw_materials",
//                    "minecraft:infested_blocks",
//                    "minecraft:animal_armors",
//                    "c:foods",
//                    "minecraft:slabs",
//                    "minecraft:doors",
//                    "minecraft:trapdoors",
//                    "minecraft:fences",
//                    "minecraft:planks",
//                    "minecraft:stairs",
//                    "minecraft:fence_gates",
//                    "minecraft:pressure_plates",
//                    "minecraft:rails",
//                    "minecraft:saplings",
//                    "minecraft:buttons",
//                    "minecraft:skulls",
//                    "minecraft:minecarts",
//                    "c:dyes",
//                    "c:ores",
//                    "minecraft:leaves",
//                    "minecraft:signs",
//                    "c:seeds",
//                    "minecraft:logs",
//                    "minecraft:hanging_signs",
//                    "c:glass_blocks",
//                    "minecraft:flowers",
//                    "minecraft:wool",
//                    "minecraft:walls",
//                    "minecraft:boats",
//                    "c:glass_panes",
//                    "minecraft:terracotta",
//                    "minecraft:banners",
//                    "minecraft:wool_carpets",
//                    "c:shulker_boxes",
//                    "c:glazed_terracottas",
//                    "c:concrete_powders",
//                    "c:concretes",
//                    "minecraft:beds",
//                    "minecraft:candles",
//                    "c:buckets",
//                    "minecraft:copper_blocks"
//                )
//            }, { "" }, { it is String })
//        }
//        builder.group("miscellaneous") {
//        }
    }

}