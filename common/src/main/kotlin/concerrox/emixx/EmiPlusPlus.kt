package concerrox.emixx

import com.mojang.logging.LogUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

fun res(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(EmiPlusPlus.MOD_ID, path)
fun text(type: String, path: String): MutableComponent = Component.translatable("$type.${EmiPlusPlus.MOD_ID}.$path")

object EmiPlusPlus {

    const val MOD_ID = "emixx"
    internal var LOGGER: Logger = LogUtils.getLogger()
    internal lateinit var PLATFORM: EmiPlusPlusPlatform

    fun initialize(platform: EmiPlusPlusPlatform) {
        PLATFORM = platform
    }

    fun initializeClient(platform: EmiPlusPlusPlatform) {
        PLATFORM = platform
    }

}