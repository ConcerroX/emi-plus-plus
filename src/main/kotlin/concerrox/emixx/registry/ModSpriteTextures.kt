package concerrox.emixx.registry

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture
import concerrox.emixx.EmiPlusPlus

object ModSpriteTextures {

    private const val SPRITES_LOCATION = "${EmiPlusPlus.MOD_ID}:textures/gui/sprites.png"

    val TAB_BACKGROUND_MODERN: SpriteTexture = sprites().setSprite(0, 0, 16, 16).setBorder(2, 4, 2, 3)
    val TAB_BACKGROUND_MODERN_HOVERED: SpriteTexture = sprites().setSprite(0, 16, 16, 16).setBorder(2, 4, 2, 3)
    val TAB_BACKGROUND_MODERN_SELECTED: SpriteTexture = sprites().setSprite(0, 32, 16, 16).setBorder(2, 4, 2, 3)
    val TAB_BACKGROUND_MODERN_SELECTED_HOVERED: SpriteTexture = sprites().setSprite(0, 48, 16, 16).setBorder(2, 4, 2, 3)

    private fun sprites() = SpriteTexture.of(SPRITES_LOCATION)

}