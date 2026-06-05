package concerrox.minecraft.emiplusplus

import net.minecraft.resources.ResourceLocation

/** Kotlin alias for [ResourceLocation] to ease future porting. */
typealias Identifier = ResourceLocation

/** Create an [Identifier] with the EMI++ mod namespace. */
fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(EmiPlusPlus.MOD_ID, path)
