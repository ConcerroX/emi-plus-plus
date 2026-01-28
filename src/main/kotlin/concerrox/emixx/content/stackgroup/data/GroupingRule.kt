package concerrox.emixx.content.stackgroup.data

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import concerrox.emixx.Identifier
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiIngredientSerializers
import dev.emi.emi.registry.EmiTags
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

sealed class GroupingRule {

    companion object {
        val CODEC: Codec<GroupingRule> = Codec.STRING.comapFlatMap({ str ->
            val ret = try {
                when {
                    str.startsWith("/") && str.endsWith("/") && str.length >= 2 -> {
                        Regex(Regex(str.substring(1, str.length - 1)))
                    }

                    str.startsWith("#") -> {
                        val parts = str.substring(1).split(":")
                        if (parts.size < 3) return@comapFlatMap DataResult.error { "Invalid Tag format: $str" }
                        Tag(
                            parts[0], ResourceLocation.fromNamespaceAndPath(parts[1], parts[2])
                        ) // TODO: mekanism chemical and more
                    }

                    else -> {
                        Stack(EmiIngredientSerializers.deserialize(JsonParser.parseString(str)))
                    }
                }
            } catch (e: Exception) {
                return@comapFlatMap DataResult.error { "Failed to parse rule '$str': ${e.message}" }
            }
            return@comapFlatMap DataResult.success(ret)
        }, { rule ->
            when (rule) {
                is Regex -> "/${rule.expression.pattern}/"
                is Tag -> "#${rule.tag.registry.location().path}:${rule.tag.location}"
                is Stack -> EmiIngredientSerializers.serialize(rule.stack).toString()
            }
        })
    }

    abstract val typeName: String
    abstract fun match(stack: EmiStack): Boolean

    data class Tag(val tag: TagKey<Any>) : GroupingRule() {
        private val tagContent = EmiTags.getRawValues(tag).toSet()

        constructor (type: String, id: ResourceLocation) : this(
            TagKey.create<Any>(ResourceKey.createRegistryKey(Identifier.parse(type)), id)
        )

        override val typeName = "tag"
        override fun match(stack: EmiStack) = stack in tagContent
    }

    data class Stack(val stack: EmiIngredient) : GroupingRule() {
        constructor(stackJson: JsonElement) : this(EmiIngredientSerializers.deserialize(stackJson))

        override val typeName = "stack"
        override fun match(stack: EmiStack) = stack == this.stack
    }

    data class Regex(val expression: kotlin.text.Regex) : GroupingRule() {
        override val typeName = "regex"
        override fun match(stack: EmiStack) = expression.matches(stack.id.toString())
    }

}