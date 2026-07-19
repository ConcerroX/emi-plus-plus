package concerrox.minecraft.emiplusplus.group

import com.google.gson.JsonPrimitive
import concerrox.minecraft.emiplusplus.Identifier
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block

/**
 * Selector that determines whether an [EmiStack] belongs to a group.
 *
 * Config notation (supports all types registered with EMI's serialization):
 * - `<type>:namespace:id` → match by stack ID (any EmiStack type)
 * - `#<type>:namespace:tag` → match by tag (uses EMI's built-in deserialization)
 * - `#block:namespace:tag` → match block tags using Minecraft's block registry
 *
 * EMI registers `item` and `fluid` by default. Block tags are resolved directly from
 * Minecraft's block registry because EmiTags does not maintain that data.
 */
sealed class GroupSelector {

    /** Match an EmiStack by its exact registry ID. Works for all EmiStack types. */
    class IdSelector(val id: Identifier) : GroupSelector() {
        override fun match(stack: EmiStack): Boolean = stack.id == id
        override fun toString(): String = "id:$id"
    }

    /** Match stacks via EMI's tag deserializer. */
    class TagSelector(val rawNotation: String) : GroupSelector() {
        private val ingredient: EmiIngredient = EmiIngredientSerializer.getDeserialized(JsonPrimitive(rawNotation))

        override fun match(stack: EmiStack): Boolean {
            return ingredient.emiStacks.any { it.isEqual(stack) }
        }

        override fun toString(): String = rawNotation
    }

    /** Match stacks by the backing Minecraft block's tag membership. */
    class BlockTagSelector(val tag: TagKey<Block>, val rawNotation: String) : GroupSelector() {
        override fun match(stack: EmiStack): Boolean {
            val item = stack.itemStack.item
            val block = when (item) {
                is BlockItem -> item.block
                else -> return false
            }
            return block.builtInRegistryHolder().`is`(tag)
        }

        override fun toString(): String = rawNotation
    }

    abstract fun match(stack: EmiStack): Boolean

    companion object {
        /**
         * Parse a selector string:
         * - `item:namespace:id` → ID match (works for any type prefix)
         * - `#item:namespace:tag` → tag match via EMI's serializer
         * - `#block:namespace:tag` → block tag match via Minecraft registry
         */
        fun parse(notation: String): GroupSelector? {
            return try {
                when {
                    notation.startsWith("#block:") -> parseBlockTag(notation)?.let { BlockTagSelector(it, notation) }
                    notation.startsWith("#") -> TagSelector(notation)
                    else -> parseId(notation)
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun parseId(notation: String): GroupSelector {
            val parts = notation.split(":")
            require(parts.size == 3) { "Invalid selector: $notation" }
            // parts[0] is the type prefix (item, fluid, etc.) — all resolve to ID match
            val id = Identifier.fromNamespaceAndPath(parts[1], parts[2])
            return IdSelector(id)
        }

        private fun parseBlockTag(notation: String): TagKey<Block>? {
            val raw = notation.removePrefix("#block:")
            val parts = raw.split(":", limit = 2)
            require(parts.size == 2) { "Invalid block tag selector: $notation" }
            val id = Identifier.fromNamespaceAndPath(parts[0], parts[1])
            return TagKey.create(Registries.BLOCK, id)
        }
    }
}
