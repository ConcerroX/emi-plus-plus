package concerrox.emixx.content.stackgroup.gui

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.StackManager
import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.text
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.tabs.GridLayoutTab
import net.minecraft.client.gui.components.tabs.TabManager
import net.minecraft.client.gui.components.tabs.TabNavigationBar
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.resources.ResourceLocation

class StackGroupConfigScreen : Screen(text("gui", "stack_group_config")) {

    private val disabledStackGroups =
        EmiPlusPlusConfig.disabledStackGroups.get().map { ResourceLocation.parse(it) }.toMutableSet()

    private val layout = HeaderAndFooterLayout(this)
    private val list = StackGroupGridList(this, disabledStackGroups)
    private val tabManager = TabManager(::addRenderableWidget, ::removeWidget)
    private val tabNavigationBar = TabNavigationBar.builder(tabManager, width).addTabs(
        PrebuiltTab()
    ).build()

    override fun init() {
        layout.addTitleHeader(title, font)
        layout.addToContents(list)
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE) {
            EmiPlusPlusConfig.disabledStackGroups.set(disabledStackGroups.map { it.toString() }.toList())
            EmiPlusPlusConfig.save()
            onClose()

            StackGroupManager.reload()
            StackManager.reload()
        }.width(200).build())
        layout.visitWidgets(::addRenderableWidget)
        repositionElements()
        list.add()
    }

    override fun repositionElements() {
        list.updateSize(width, layout)
        //        list.width = width
//        list.height = layout.contentHeight
        layout.arrangeElements()
        tabNavigationBar.setWidth(width)
        tabNavigationBar.arrangeElements()
        tabManager.setTabArea(ScreenRectangle(0, tabNavigationBar.rectangle.bottom(), width, height))
    }

    private class PrebuiltTab : GridLayoutTab(text("gui", "stack_group_config.prebuilt"))

}