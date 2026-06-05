package concerrox.minecraft.emiplusplus.group

import com.google.gson.JsonPrimitive
import concerrox.minecraft.emiplusplus.Identifier
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer

/**
 * Selector that determines whether an [EmiStack] belongs to a group.
 *
 * Config notation (supports all types registered with EMI's serialization):
 * - `<type>:namespace:id` → match by stack ID (any EmiStack type)
 * - `#<type>:namespace:tag` → match by tag (uses EMI's built-in deserialization)
 *
 * EMI registers `item` and `fluid` by default. Other mods may register more.
 */
sealed class GroupSelector {

    /** Match an EmiStack by its exact registry ID. Works for all EmiStack types. */
    class IdSelector(val id: Identifier) : GroupSelector() {
        override fun match(stack: EmiStack): Boolean = stack.id == id
        override fun toString(): String = "id:$id"
    }

    /**
     * Match stacks via a tag. Uses EMI's own [EmiIngredientSerializer] to parse
     * the notation, so any tag type EMI supports (item, fluid, mod-added) works.
     */
    class TagSelector(val rawNotation: String) : GroupSelector() {
        private val ingredient: EmiIngredient =
            EmiIngredientSerializer.getDeserialized(JsonPrimitive(rawNotation))

        override fun match(stack: EmiStack): Boolean {
            return ingredient.emiStacks.any { it.isEqual(stack) }
        }
        override fun toString(): String = rawNotation
    }

    abstract fun match(stack: EmiStack): Boolean

    companion object {
        /**
         * Parse a selector string:
         * - `item:namespace:id` → ID match (works for any type prefix)
         * - `#item:namespace:tag` → tag match via EMI's serializer
         */
        fun parse(notation: String): GroupSelector? {
            return try {
                if (notation.startsWith("#")) TagSelector(notation)
                else parseId(notation)
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
    }
}
