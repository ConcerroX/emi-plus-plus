package concerrox.emixx.content.stackgroup.data

import concerrox.blueberry.registry.TranslationKey
import concerrox.emixx.Identifier
import concerrox.emixx.registry.ModLang
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import dev.emi.emi.stack.serializer.FluidEmiStackSerializer
import dev.emi.emi.stack.serializer.ItemEmiStackSerializer
import mekanism.api.MekanismAPI
import mekanism.client.recipe_viewer.emi.ChemicalEmiIngredientSerializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.neoforged.fml.ModList

data class RegistryToken<T, S : EmiIngredient>(
    val key: ResourceKey<out Registry<T>>, val ingredientSerializer: EmiIngredientSerializer<S>, val translationKey: TranslationKey
) {
    val id: Identifier get() = key.location()
    val serializationType: String get() = ingredientSerializer.type

    @Suppress("unchecked_cast")
    val registry: Registry<T> get() = BuiltInRegistries.REGISTRY.get(id) as Registry<T>
}

object RegistryTokens {

    private val BLOCK_STACK_SERIALIZER = object : ItemEmiStackSerializer() {
        override fun getType() = "block"
    }
    private val tokens = mutableListOf<RegistryToken<*, *>>()

    val ITEM = register(RegistryToken(Registries.ITEM, ItemEmiStackSerializer(), ModLang.item))
    val BLOCK = register(RegistryToken(Registries.BLOCK, BLOCK_STACK_SERIALIZER, ModLang.block))
    val FLUID = register(RegistryToken(Registries.FLUID, FluidEmiStackSerializer(), ModLang.fluid))

    val MEKANISM_CHEMICAL = register(modLoaded("mekanism")) {
        RegistryToken(MekanismAPI.CHEMICAL_REGISTRY_NAME, ChemicalEmiIngredientSerializer(), ModLang.chemical)
    }

    fun <T, S : EmiIngredient> register(token: RegistryToken<T, S>): RegistryToken<T, S> {
        tokens += token
        return token
    }

    fun register(condition: () -> Boolean, tokenBuilder: () -> RegistryToken<*, *>): RegistryToken<*, *>? {
        if (condition()) {
            val token = tokenBuilder()
            tokens += token
            return token
        }
        return null
    }

    fun getByKey(key: ResourceKey<*>?): RegistryToken<*, *>? {
        return tokens.firstOrNull { it.key == key }
    }

    fun getById(id: Identifier?): RegistryToken<*, *>? {
        return tokens.firstOrNull { it.id == id }
    }

    fun getBySerializationType(serializationType: String?): RegistryToken<*, *>? {
        return tokens.firstOrNull { it.serializationType == serializationType }
    }

    fun listTokens(): List<RegistryToken<*, *>> {
        return tokens.toList()
    }

    private fun modLoaded(modId: String): () -> Boolean {
        return { ModList.get().isLoaded(modId) }
    }

}