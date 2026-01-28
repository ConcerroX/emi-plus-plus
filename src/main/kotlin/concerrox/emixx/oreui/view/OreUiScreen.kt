package concerrox.emixx.oreui.view

import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.screen.ScreenManager
import concerrox.emixx.mixin.ScreenAccessor
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

@Deprecated("")
fun <T> Screen.addAutoWidget(widgets: T) where T : GuiEventListener, T : Renderable, T : NarratableEntry {
    (this as ScreenAccessor).addRenderableWidgetExternal(widgets)
    if (widgets is AutoInitializable) widgets.onInitialized(this)
}

@Deprecated("")
fun <T> Screen.addAutoWidgetNoRender(widgets: T) where T : GuiEventListener, T : NarratableEntry {
    (this as ScreenAccessor).addWidgetExternal(widgets)
    if (widgets is AutoInitializable) widgets.onInitialized(this)
}

@Deprecated("")
abstract class OreUiScreen(title: Component) : Screen(title) {

    abstract val viewModel: ViewModel

    abstract fun onBindData()
    abstract fun onCreated()
    abstract fun onCreateView()

    override fun onClose() {
        ScreenManager.popScreen()
    }

    override fun added() {
        onBindData()
        onCreated()
    }

    override fun init() {
        onCreateView()
    }

}