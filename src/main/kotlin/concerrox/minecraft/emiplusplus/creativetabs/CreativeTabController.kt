package concerrox.minecraft.emiplusplus.creativetabs

import com.mojang.logging.LogUtils
import concerrox.minecraft.emiplusplus.config.EmiPlusPlusConfig
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.ItemStack
import org.slf4j.Logger

data class CreativeTabEntry(
    val creativeModeTab: CreativeModeTab?,
    val title: Component,
    val icon: ItemStack,
    val isIndex: Boolean = false,
)

object CreativeTabController {

    private val LOGGER: Logger = LogUtils.getLogger()

    private val indexCreativeModeTab by lazy {
        BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH)
    }

    private val hiddenTabs by lazy {
        setOf(
            BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.INVENTORY),
            BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.HOTBAR),
            BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH)
        )
    }

    private var state = CreativeTabState()

    fun reload() {
        val entries = BuiltInRegistries.CREATIVE_MODE_TAB
            .toList()
            .filterNot { it in hiddenTabs || !it.shouldDisplay() }
            .map { CreativeTabEntry(it, it.displayName, it.iconItem, false) }
            .toMutableList()
            .apply {
                add(
                    0,
                    CreativeTabEntry(indexCreativeModeTab, indexCreativeModeTab!!.displayName, indexCreativeModeTab!!.iconItem, true)
                )
            }

        val selected = state.selectedTab?.takeIf { it in entries } ?: entries.firstOrNull()
        state = state.copy(tabs = entries, selectedTab = selected).clampPage()
        LOGGER.info("Loaded {} visible creative tabs", entries.size)
    }

    fun tabs(): List<CreativeTabEntry> = state.tabs

    fun selectedTab(): CreativeTabEntry? = state.selectedTab

    fun currentTabPage(): Int = state.currentTabPage

    fun selectTab(tab: CreativeTabEntry?) {
        if (tab == null || tab == state.selectedTab) return
        state = state.copy(selectedTab = tab)
    }

    fun pageSize(maxTabsPerPage: Int): Int = maxTabsPerPage.coerceAtLeast(1)

    fun tabCountPerPage(): Int = 8

    fun visibleTabsForPage(maxTabsPerPage: Int = tabCountPerPage()): List<CreativeTabEntry> {
        val size = pageSize(maxTabsPerPage)
        val start = state.currentTabPage * size
        return state.tabs.drop(start).take(size)
    }

    fun hasMultiplePages(maxTabsPerPage: Int = tabCountPerPage()): Boolean = state.tabs.size > pageSize(maxTabsPerPage)

    fun nextPage(maxTabsPerPage: Int = tabCountPerPage()) {
        val totalPages = totalPages(maxTabsPerPage)
        if (totalPages <= 1) return
        val next = if (state.currentTabPage + 1 >= totalPages) 0 else state.currentTabPage + 1
        state = state.copy(currentTabPage = next)
    }

    fun previousPage(maxTabsPerPage: Int = tabCountPerPage()) {
        val totalPages = totalPages(maxTabsPerPage)
        if (totalPages <= 1) return
        val prev = if (state.currentTabPage - 1 < 0) totalPages - 1 else state.currentTabPage - 1
        state = state.copy(currentTabPage = prev)
    }

    fun totalPages(maxTabsPerPage: Int = tabCountPerPage()): Int {
        val size = pageSize(maxTabsPerPage)
        return maxOf(1, (state.tabs.size + size - 1) / size)
    }

    fun currentBaseStacks(): List<EmiStack> {
        val selected = state.selectedTab ?: return EmiStackList.stacks
        return if (selected.isIndex || selected.creativeModeTab == null) {
            EmiStackList.stacks
        } else {
            selected.creativeModeTab.displayItems.map(EmiStack::of)
        }
    }

    fun isEnabled(): Boolean = EmiPlusPlusConfig.creativeModeTabsEnabled

    private fun CreativeTabState.clampPage(maxTabsPerPage: Int = tabCountPerPage()): CreativeTabState {
        val totalPages = maxOf(1, (tabs.size + maxTabsPerPage - 1) / maxTabsPerPage)
        return copy(currentTabPage = currentTabPage.coerceIn(0, totalPages - 1))
    }
}
