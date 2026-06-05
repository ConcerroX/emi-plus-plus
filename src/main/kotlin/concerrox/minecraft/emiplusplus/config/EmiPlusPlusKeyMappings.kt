package concerrox.minecraft.emiplusplus.config

import com.mojang.blaze3d.platform.InputConstants
import dev.emi.emi.input.EmiBind
import dev.emi.emi.input.EmiInput

/**
 * EMI++ keybindings registered with EMI's config system.
 */
object EmiPlusPlusKeyMappings {

    /** Alt + Left Click on a grouped member collapses the parent group. */
    @JvmField
    val collapseGroup: EmiBind = EmiBind(
        "emixx.key.collapseGroup",
        EmiBind.ModifiedKey(InputConstants.Type.MOUSE.getOrCreate(0), EmiInput.ALT_MASK)
    )
}
