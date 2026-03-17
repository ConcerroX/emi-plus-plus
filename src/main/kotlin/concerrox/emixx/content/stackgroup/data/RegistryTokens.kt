package concerrox.emixx.content.stackgroup.data

import concerrox.blueberry.registry.TranslationKey
import concerrox.emixx.Identifier
import concerrox.emixx.registry.ModLang
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.FluidEmiStack
import dev.emi.emi.api.stack.ItemEmiStack
import dev.emi.emi.api.stack.serializer.EmiStackSerializer
import dev.emi.emi.stack.serializer.FluidEmiStackSerializer
import dev.emi.emi.stack.serializer.ItemEmiStackSerializer
import mekanism.api.MekanismAPI
import mekanism.client.recipe_viewer.emi.ChemicalEmiIngredientSerializer
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.neoforged.fml.ModList

data class RegistryToken<T, S : EmiStack>(
    val key: ResourceKey<out Registry<T>>,
    val stackClass: Class<S>,
    val stackSerializer: EmiStackSerializer<S>,
    val translationKey: TranslationKey
) {
    val id: Identifier get() = key.location()
    val serializationType: String get() = stackSerializer.type

    @Suppress("unchecked_cast")
    val registry: Registry<T> get() = BuiltInRegistries.REGISTRY.get(id) as Registry<T>

    fun isIn(stack: EmiStack): Boolean {
        return stackClass.isInstance(stack) && (if (this === RegistryTokens.BLOCK) stack.key is BlockItem else true)
    }
}

@Suppress("unstableApiUsage")
object RegistryTokens {

    private val BLOCK_STACK_SERIALIZER = object : ItemEmiStackSerializer() {
        override fun getType() = "block"
    }
    private val tokens = mutableListOf<RegistryToken<*, *>>()

    val ITEM =
        register(RegistryToken(Registries.ITEM, ItemEmiStack::class.java, ItemEmiStackSerializer(), ModLang.item))
    val BLOCK =
        register(RegistryToken(Registries.BLOCK, ItemEmiStack::class.java, BLOCK_STACK_SERIALIZER, ModLang.block))
    val FLUID =
        register(RegistryToken(Registries.FLUID, FluidEmiStack::class.java, FluidEmiStackSerializer(), ModLang.fluid))

    val MEKANISM_CHEMICAL = register(modLoaded("mekanism")) {
        RegistryToken(
            MekanismAPI.CHEMICAL_REGISTRY_NAME,
            ChemicalEmiStack::class.java,
            ChemicalEmiIngredientSerializer(),
            ModLang.chemical
        )
    }

    fun <T, S : EmiStack> register(token: RegistryToken<T, S>): RegistryToken<T, S> {
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