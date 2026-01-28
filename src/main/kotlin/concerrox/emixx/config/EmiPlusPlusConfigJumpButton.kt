package concerrox.emixx.config

import concerrox.emixx.id
import dev.emi.emi.screen.widget.config.ConfigJumpButton
import net.minecraft.network.chat.Component

class EmiPlusPlusConfigJumpButton(
    x: Int, y: Int, action: OnPress, text: MutableList<Component>,
) : ConfigJumpButton(x, y, 0, 0, action, text) {

    init {
        texture = id("textures/gui/jump_button.png")
    }

}