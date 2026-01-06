package concerrox.emixx.util

import net.minecraft.client.gui.components.AbstractWidget

fun AbstractWidget.pos(x: Int, y: Int) = apply {
    this.x = x
    this.y = y
}