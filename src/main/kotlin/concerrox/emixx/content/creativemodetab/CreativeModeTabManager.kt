package concerrox.emixx.content.creativemodetab

import concerrox.emixx.Minecraft
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.ScreenManager
import concerrox.emixx.content.StackManager
import concerrox.emixx.content.creativemodetab.gui.CreativeModeTabGui
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTab
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs

object CreativeModeTabManager {

    private val HIDDEN_CREATIVE_MODE_TABS = listOf(
        CreativeModeTabs.INVENTORY, CreativeModeTabs.HOTBAR, CreativeModeTabs.OP_BLOCKS, CreativeModeTabs.SEARCH
    ).map(BuiltInRegistries.CREATIVE_MODE_TAB::get)

    private var currentTabPage = 0u
    private var lastTabPage = 0u
    private var currentTab = 0u

    private var isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = false
    private var isSelectingEmiPlusPlusCreativeModeTabByVanilla = false

    private val indexCreativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH)
    private val creativeModeTabs = BuiltInRegistries.CREATIVE_MODE_TAB.toMutableList().apply {
        removeIf { it in HIDDEN_CREATIVE_MODE_TABS }
        addFirst(indexCreativeModeTab)
    }

    internal fun initialize() {
        lastTabPage = creativeModeTabs.size.toUInt() / CreativeModeTabGui.tabCount
        currentTabPage = 0u
        val page = updateTabs()

        if (page.isEmpty()) return // TODO: fix this

        // TODO: fix the sound
        // Select the index tab by default
        CreativeModeTabGui.selectTab(0, false)
        onTabSelected(CreativeModeTabGui.tabNavigationBar.tabs[0] as ItemTab)
    }

    @Suppress("unused_parameter")
    internal fun nextPage(button: Button? = null) {
        if (currentTabPage < lastTabPage) currentTabPage++ else currentTabPage = 0u
        updateTabs()
    }

    @Suppress("unused_parameter")
    internal fun previousPage(button: Button? = null) {
        if (currentTabPage > 0u) currentTabPage-- else currentTabPage = lastTabPage
        updateTabs()
    }

    internal fun onTabSelected(tab: ItemTab) {
        if (!Minecraft.isSameThread) {
            Minecraft.execute { onTabSelected(tab) }
            return
        }
        // TODO: refactor this
        currentTab = CreativeModeTabGui.tabNavigationBar.tabs.indexOf(tab).toUInt()

        CreativeModeTabGui.tabNavigationBar.tabButtons.forEachIndexed { i, it ->
            if (i.toUInt() != currentTab) it.isFocused = false
        }

        val screen = Minecraft.screen
        // Pass if it's selected by clicking the tab bar from vanilla
        if (!isSelectingEmiPlusPlusCreativeModeTabByVanilla && EmiPlusPlusConfig.syncSelectedCreativeModeTab.get() && tab.creativeModeTab != null && screen is CreativeModeInventoryScreen) {
            isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = true
            screen.selectTab(tab.creativeModeTab)
            screen.searchBox.setCanLoseFocus(true)
            screen.searchBox.isFocused = false
            isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = false
        }

        val sourceStacks = if (tab.creativeModeTab == indexCreativeModeTab) StackManager.indexStacks else {
            // Tabs with null creativeModeTab will not be selected as it's not visible.
            tab.creativeModeTab!!.displayItems.map(EmiStack::of)
        }
        if (ScreenManager.isSearching) {
            StackManager.search(sourceStacks, EmiScreenManager.search.value)
        } else {
            StackManager.updateSourceStacks(sourceStacks)
        }
    }

    internal fun onCreativeModeInventoryScreenTabSelected(tab: CreativeModeTab) {
        val screen = Minecraft.screen as? CreativeModeInventoryScreen ?: return

        if (!EmiPlusPlusConfig.syncSelectedCreativeModeTab.get()) return
        var notHiddenTab = tab
        // Pass if it's selected by clicking the tab bar from EMI++
        if (isSelectingVanillaCreativeInventoryTabByEmiPlusPlus) return
        if (notHiddenTab in HIDDEN_CREATIVE_MODE_TABS && indexCreativeModeTab != null) {
            notHiddenTab = indexCreativeModeTab // If the tab is hidden in EMI++, select the index tab
        }
        for (i in 0u..lastTabPage) {
            val tabIndex = getTabPage(i).indexOfFirst { it.creativeModeTab == notHiddenTab }
            if (tabIndex != -1) {
                currentTabPage = i
                val page = updateTabs()
                CreativeModeTabGui.selectTab(tabIndex, false)
                isSelectingEmiPlusPlusCreativeModeTabByVanilla = true
                onTabSelected(page[tabIndex])
                screen.searchBox.setCanLoseFocus(true)
                screen.searchBox.isFocused = false
                isSelectingEmiPlusPlusCreativeModeTabByVanilla = false
                return
            }
        }
    }

    private fun getTabPage(page: UInt): MutableList<ItemTab> {
        return MutableList(CreativeModeTabGui.tabCount.toInt()) { i ->
            ItemTab(creativeModeTabs.getOrNull((page * CreativeModeTabGui.tabCount).toInt() + i))
        }
    }

    private fun updateTabs(): List<ItemTab> {
        val page = getTabPage(currentTabPage)
        // TODO: check this
        val safePage = page.toMutableList()
        CreativeModeTabGui.tabNavigationBar.setTabs(safePage)
        CreativeModeTabGui.tabNavigationBar.arrangeElements()
        return safePage
    }

}