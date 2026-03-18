package concerrox.emixx.config

import com.mojang.blaze3d.platform.InputConstants
import concerrox.emixx.registry.ModLang
import dev.emi.emi.input.EmiBind
import dev.emi.emi.input.EmiBind.ModifiedKey
import dev.emi.emi.input.EmiInput

object EmiPlusPlusKeyMappings {

    val collapseGroup = EmiBind(
        ModLang.collapseStackGroup.key, ModifiedKey(InputConstants.Type.MOUSE.getOrCreate(0), EmiInput.ALT_MASK)
    )

}