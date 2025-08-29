package concerrox.emixx

import com.google.gson.JsonElement
import com.mojang.logging.LogUtils
import concerrox.emixx.content.stackgroup.GroupedEmiStack
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import dev.emi.emi.api.stack.serializer.EmiStackSerializer
import dev.emi.emi.registry.EmiIngredientSerializers
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

internal val Minecraft = net.minecraft.client.Minecraft.getInstance()

fun res(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(EmiPlusPlus.MOD_ID, path)

fun text(type: String, path: String): MutableComponent = Component.translatable("$type.${EmiPlusPlus.MOD_ID}.$path")

fun text(type: String, path: String, vararg args: Any): MutableComponent =
    Component.translatable("$type.${EmiPlusPlus.MOD_ID}.$path", args)

fun text(path: String): MutableComponent = Component.translatable("${EmiPlusPlus.MOD_ID}.$path")

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