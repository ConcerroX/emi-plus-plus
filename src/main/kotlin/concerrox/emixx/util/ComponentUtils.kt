package concerrox.emixx.util

import net.minecraft.network.chat.Component

@Deprecated("Use ModTranslationKeys instead")
internal fun text(key: String) = Component.translatable(key)