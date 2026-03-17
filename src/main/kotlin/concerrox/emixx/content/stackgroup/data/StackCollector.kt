package concerrox.emixx.content.stackgroup.data

import concerrox.emixx.Minecraft
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

object StackCollector {

    @Suppress("unchecked_cast")
    fun collectTagContent(token: RegistryToken<out Any?, out EmiStack>, tag: TagKey<*>): List<EmiStack> {
        val access = Minecraft.level?.registryAccess() ?: throw IllegalStateException("No registry access")
        val registry = access.registry(tag.registry).orElseThrow()

        val ret = mutableListOf<EmiStack>()
        for (stack in EmiStackList.stacks) {
            val key = ResourceKey.create(token.key, stack.id) as ResourceKey<Any>
            if (!registry.contains(stack.key)) continue
            if (registry.getHolder(stack.id).orElseThrow().`is`(tag as TagKey<Any>)) ret += stack
        }
        return ret
    }

}