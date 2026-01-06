package concerrox.emixx.oreui.view

import concerrox.emixx.Minecraft
import net.minecraft.client.gui.screens.Screen
import java.util.Stack

object ScreenManager {

    private var screenStack = Stack<Screen?>()

    fun pushScreen(screen: Screen) {
        screenStack.push(Minecraft.screen)
        Minecraft.setScreen(screen)
    }

    fun popScreen() {
        Minecraft.setScreen(screenStack.pop())
    }

}