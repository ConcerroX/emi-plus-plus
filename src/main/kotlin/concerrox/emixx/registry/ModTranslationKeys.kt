package concerrox.emixx.registry

import concerrox.emixx.EmiPlusPlus
import concerrox.mochests.data.ModLanguageProvider
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.data.event.GatherDataEvent

object ModTranslationKeys {

    internal val TRANSLATION_KEYS = mutableSetOf<TranslationKey>()

    val CONTAINER_IRON_CHEST = create("container", "iron_chest", "Iron Chest")

    internal fun register() {}

    private fun create(type: String, key: String, defaultValue: String): TranslationKey {
        return TranslationKey("$type.${EmiPlusPlus.MOD_ID}.$key", defaultValue).also { TRANSLATION_KEYS += it }
    }

    data class TranslationKey(val key: String, val defaultValue: String) {
        fun toComponent(): Component = Component.translatable(key)
    }

}