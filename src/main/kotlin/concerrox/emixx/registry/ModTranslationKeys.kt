package concerrox.emixx.registry

import concerrox.emixx.EmiPlusPlus
import net.minecraft.network.chat.Component

object ModTranslationKeys {

    internal val TRANSLATION_KEYS = mutableSetOf<TranslationKey>()

    object Config {
        val UI = createConfig("ui", "UI")

        val INDEX_SIDEBAR_HEADER_WIDGET = createConfig("indexSidebarHeaderWidget", "Index Sidebar Header Widget")
        val INDEX_SIDEBAR_FOOTER_WIDGET = createConfig("indexSidebarFooterWidget", "Index Sidebar Footer Widget")

        val INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_NONE = createConfig(
            "indexSidebarHeaderFooterWidget.none", "None",
        )
        val INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_CREATIVE_MODE_TABS = createConfig(
            "indexSidebarHeaderFooterWidget.creativeModeTabs", "Creative Mode Tabs",
        )
        val INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_CUSTOM_TABS = createConfig(
            "indexSidebarHeaderFooterWidget.customTabs", "Custom Tabs",
        )
        val INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_CREATIVE_MODE_AND_CUSTOM_TABS = createConfig(
            "indexSidebarHeaderFooterWidget.creativeModeAndCustomTabs", "Creative Mode and Custom Tabs",
        )
        val INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_SEARCH_BOX = createConfig(
            "indexSidebarHeaderFooterWidget.searchBox", "Search Box",
        )

        val INDEX_SIDEBAR_TAB_BUTTON_SIZE = createConfig("indexSidebarTabButtonSize", "Index Sidebar Tab Button Size")
        val HIDE_PAGINATION_BUTTONS = createConfig("hidePaginationButtons", "Hide Pagination Buttons")

        val STACK_GROUPS = createConfig("stackGroups", "Stack Groups")
        val STACK_GROUP_CONFIG = createConfig("stackGroupConfig", "Stack Group Config")
        val STACK_GROUP_CONFIG_EDIT = createConfig("stackGroupConfig.edit", "Edit")
        val STACK_GROUP_CONFIG_CREATE = createConfig("stackGroupConfig.create", "Create")
        val STACK_GROUP_CONFIG_TAB_ALL = createConfig("stackGroupConfig.tab.all", "All")
        val STACK_GROUP_CONFIG_TAB_APPLICABLE = createConfig("stackGroupConfig.tab.applicable", "Applicable")
        val STACK_GROUP_CONFIG_TAB_NOT_APPLICABLE = createConfig("stackGroupConfig.tab.not_applicable", "Not Applicable")

        private fun createConfig(key: String, defaultValue: String): TranslationKey {
            return TranslationKey(
                "${EmiPlusPlus.MOD_ID}.configuration.$key", defaultValue
            ).also { TRANSLATION_KEYS += it }
        }
    }

    internal fun register() {}

    private fun create(type: String, key: String, defaultValue: String): TranslationKey {
        return TranslationKey("$type.${EmiPlusPlus.MOD_ID}.$key", defaultValue).also { TRANSLATION_KEYS += it }
    }

    data class TranslationKey(val key: String, val defaultValue: String) {
        fun asComponent(): Component = Component.translatable(key)
    }

}