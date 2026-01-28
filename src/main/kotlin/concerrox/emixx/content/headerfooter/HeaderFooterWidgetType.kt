package concerrox.emixx.content.headerfooter

import concerrox.emixx.registry.ModTranslationKeys
import dev.emi.emi.config.ConfigEnum

enum class HeaderFooterWidgetType(private val translationKey: ModTranslationKeys.TranslationKey, val sizeHeight: Int) :
    ConfigEnum {

    NONE(ModTranslationKeys.Config.INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_NONE, 20),
    CREATIVE_MODE_TABS(ModTranslationKeys.Config.INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_CREATIVE_MODE_TABS, 14),
    CUSTOM_TABS(ModTranslationKeys.Config.INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_CUSTOM_TABS, 20),
    CREATIVE_MODE_AND_CUSTOM_TABS(ModTranslationKeys.Config.INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_CREATIVE_MODE_AND_CUSTOM_TABS, 20),
    SEARCH_BOX(ModTranslationKeys.Config.INDEX_SIDEBAR_HEADER_FOOTER_WIDGET_SEARCH_BOX, 20);

    override fun getName() = name
    override fun getText() = translationKey.asComponent()

}