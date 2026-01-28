package concerrox.emixx.content.headerfooter

import concerrox.emixx.config.EmiPlusPlusConfig

object HeaderFooterHelper {

    val headerType: HeaderFooterWidgetType get() = EmiPlusPlusConfig.indexSidebarHeaderWidget.get()
    val footerType: HeaderFooterWidgetType get() = EmiPlusPlusConfig.indexSidebarFooterWidget.get()

    internal fun createHeader() = createHeaderFooter(isHeader = true)
    internal fun createFooter() = createHeaderFooter(isHeader = false)

    private fun createHeaderFooter(isHeader: Boolean) = when (if (isHeader) headerType else footerType) {
        else -> TabHeaderFooter(isHeader)
    }

}