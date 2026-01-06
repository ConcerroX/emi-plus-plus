package concerrox.emixx.content.creativemodetab.gui.itemtab

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.tabs.TabManager
import java.util.function.Consumer

class ItemTabManager(addWidget: Consumer<AbstractWidget>,
    removeWidget: Consumer<AbstractWidget>
) : TabManager(addWidget, removeWidget) {

    var onTabSelectedListener: ((ItemTab) -> Unit)? = null

    fun onTabSelected(tab: ItemTab) {
        onTabSelectedListener?.invoke(tab)
    }

}