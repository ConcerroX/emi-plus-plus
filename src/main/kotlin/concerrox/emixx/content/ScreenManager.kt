package concerrox.emixx.content

import concerrox.blueberry.ui.screen.ScreenManager
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.content.creativemodetab.gui.CreativeModeTabGui
import concerrox.emixx.content.stackgroup.ui.StackGroupConfigScreenV2
import concerrox.emixx.mixin.ScreenAccessor
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.text.isNotEmpty

object ScreenManager {

    const val ENTRY_SIZE = 18

    lateinit var screen: Screen
    var indexScreenSpace: EmiScreenManager.ScreenSpace? = null

    internal val isSearching
        get() = indexScreenSpace?.search == true && EmiScreenManager.search.value.isNotEmpty()

    private val isCreativeModeTabEnabled
        get() = EmiPlusPlusConfig.enableCreativeModeTabs.get()

    internal var customIndexTitle: Component? = null

    fun onScreenInitialized(screen: Screen) {
        this.screen = screen
        (screen as ScreenAccessor).addRenderableWidgetExternal(Button.builder(Component.literal("EMI++")) {
            ScreenManager.pushScreen(StackGroupConfigScreenV2())
        }.build())
    }

    fun onIndexScreenSpaceCreated(indexScreenSpace: EmiScreenManager.ScreenSpace) {
        this.indexScreenSpace = indexScreenSpace

        if (isCreativeModeTabEnabled) {
            CreativeModeTabGui.initialize(screen)
            CreativeModeTabManager.initialize()
        }
    }

    fun onMouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        return if (isCreativeModeTabEnabled && indexScreenSpace != null && CreativeModeTabGui.contains(
                mouseX, mouseY
            )
        ) {
            CreativeModeTabGui.onMouseScrolled(mouseX, mouseY, amount)
        } else false
    }

    fun removeCustomIndexTitle(component: Component?) {
        if (customIndexTitle == component) customIndexTitle = null
    }
}