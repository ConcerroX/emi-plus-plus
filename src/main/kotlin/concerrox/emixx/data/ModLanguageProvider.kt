package concerrox.emixx.data

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.registry.ModTranslationKeys
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

@Deprecated("")
class ModLanguageProvider(output: PackOutput) : LanguageProvider(output, EmiPlusPlus.MOD_ID, "en_us") {

    override fun addTranslations() {
        ModTranslationKeys.TRANSLATION_KEYS.forEach { add(it.key, it.defaultValue) }
    }

}