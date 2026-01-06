package concerrox.emixx.oreui

import concerrox.emixx.id
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

object OreUi {

    val CARD_START_SPRITE = id("card/start")
    val CARD_END_SPRITE = id("card/end")

    const val SPACING_SMALL = 4
    const val SPACING_MEDIUM = 8
    const val SPACING_LARGE = 16

    private val TITLE_FONT_IDENTIFIER = id("minecraft_ten")

    fun createTitleComponent(text: Component): MutableComponent {
        return text.copy().withStyle(Style.EMPTY.withFont(TITLE_FONT_IDENTIFIER))
    }

}