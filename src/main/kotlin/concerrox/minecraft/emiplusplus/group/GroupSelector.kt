package concerrox.minecraft.emiplusplus.group

import concerrox.minecraft.emiplusplus.Identifier
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.tags.TagKey
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item

/**
 * Selector that determines whether an [EmiStack] belongs to a group.
 *
 * Parsed from string notation:
 * - `item:namespace:id` → exact item match by EmiStack.getId()
 * - `#item:namespace:tag` → item tag match
 */
sealed class GroupSelector {

    /** Match an EmiStack by its exact registry ID. */
    class ItemSelector(val id: Identifier) : GroupSelector() {
        override fun match(stack: EmiStack): Boolean = stack.id == id
        override fun toString(): String = "item:$id"
    }

    /** Match all EmiStacks whose item is in the given tag. */
    class TagSelector(tag: TagKey<Item>) : GroupSelector() {
        val tagId: Identifier = tag.location
        private val tagIngredient: EmiIngredient = EmiIngredient.of(tag)

        override fun match(stack: EmiStack): Boolean {
            return tagIngredient.emiStacks.any { it.isEqual(stack) }
        }
        override fun toString(): String = "#item:$tagId"
    }

    abstract fun match(stack: EmiStack): Boolean

    companion object {
        /**
         * Parse a selector string like "item:minecraft:boat" or "#item:minecraft:planks".
         * Returns null if the format is invalid.
         */
        fun parse(notation: String): GroupSelector? {
            return try {
                when {
                    notation.startsWith("#") -> parseTag(notation)
                    else -> parseItem(notation)
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun parseItem(notation: String): GroupSelector {
            // Format: "item:namespace:id"
            val parts = notation.split(":")
            require(parts.size == 3 && parts[0] == "item") { "Invalid item selector: $notation" }
            val id = Identifier.fromNamespaceAndPath(parts[1], parts[2])
            return ItemSelector(id)
        }

        private fun parseTag(notation: String): GroupSelector {
            // Format: "#item:namespace:tag"
            val stripped = notation.removePrefix("#")
            val parts = stripped.split(":")
            require(parts.size == 3 && parts[0] == "item") { "Invalid tag selector: $notation" }
            val id = Identifier.fromNamespaceAndPath(parts[1], parts[2])
            return TagSelector(TagKey.create(Registries.ITEM, id))
        }
    }
}
