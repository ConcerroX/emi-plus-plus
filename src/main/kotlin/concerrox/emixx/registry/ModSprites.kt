package concerrox.emixx.registry

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture
import concerrox.emixx.EmiPlusPlus

object ModSprites {

    private const val SPRITES_LOCATION = "${EmiPlusPlus.MOD_ID}:textures/gui/sprites.png"

    val TAB_BACKGROUND_MODERN: SpriteTexture = sprites().setSprite(0, 0, 16, 16).setBorder(2, 4, 2, 3)
    val TAB_BACKGROUND_MODERN_HOVERED: SpriteTexture = sprites().setSprite(0, 16, 16, 16).setBorder(2, 4, 2, 3)
    val TAB_BACKGROUND_MODERN_SELECTED: SpriteTexture = sprites().setSprite(0, 32, 16, 16).setBorder(2, 4, 2, 3)
    val TAB_BACKGROUND_MODERN_SELECTED_HOVERED: SpriteTexture = sprites().setSprite(0, 48, 16, 16).setBorder(2, 4, 2, 3)

    val STACK_GROUP_PREVIEW_BACKGROUND: SpriteTexture = sprites().setSprite(16, 0, 16, 16).setBorder(2, 2, 2, 2)

    val ICON_GROUPING_RULE_TAG: SpriteTexture = sprites().setSprite(16, 32, 16, 16)
    val ICON_GROUPING_RULE_IDENTIFIER: SpriteTexture = sprites().setSprite(16, 80, 16, 16)
    val ICON_GROUPING_RULE_STACK: SpriteTexture = sprites().setSprite(16, 16, 16, 16)
    val ICON_GROUPING_RULE_REGEX: SpriteTexture = sprites().setSprite(16, 48, 16, 16)

    val ICON_DELETE: SpriteTexture = sprites().setSprite(16, 64, 16, 16)

    private fun sprites() = SpriteTexture.of(SPRITES_LOCATION)

}