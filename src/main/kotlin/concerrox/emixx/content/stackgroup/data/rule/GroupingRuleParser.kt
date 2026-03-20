package concerrox.emixx.content.stackgroup.data.rule

import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import concerrox.emixx.content.stackgroup.data.RegistryToken
import concerrox.emixx.content.stackgroup.data.RegistryTokens
import concerrox.emixx.id
import concerrox.emixx.util.logError
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiIngredientSerializers
import net.minecraft.tags.TagKey

object GroupingRuleParser {

    val CODEC: Codec<GroupingRule> = Codec.STRING.xmap(::parse) { it?.encode() }

    fun parse(notation: String): GroupingRule? = try {
        if (notation.startsWith('#')) {
            parseTagRule(notation)
        } else if (notation.startsWith('&')) {
            parseIdentifierRule(notation)
        } else if (notation.startsWith('/') && notation.endsWith('/')) {
            parseRegexRule(notation)
        } else {
            parseStackRule(notation)
        }
    } catch (e: IllegalArgumentException) {
        logError("Error parsing grouping rule: $notation", e)
        null
    } catch (_: NoSuchElementException) {
        // TODO: warnings
        null // Missing registry entry
    }

    @Throws(IllegalArgumentException::class)
    private fun splitParts(notation: String): List<String> {
        val parts = notation.split(':')
        if (parts.size != 3) throw IllegalArgumentException("Invalid Stack format: $notation")
        return parts
    }

    @Throws(IllegalArgumentException::class)
    private fun parseToken(tokenType: String): RegistryToken<*, *> {
        return RegistryTokens.getBySerializationType(tokenType)
            ?: throw IllegalArgumentException("Invalid token type: $tokenType")
    }

    @Throws(IllegalArgumentException::class)
    private fun parseTagRule(notation: String): GroupingRule.Tag {
        val (tokenType, namespace, path) = splitParts(notation)
        val token = parseToken(tokenType.trimStart('#'))
        return GroupingRule.Tag(token, TagKey.create(token.key, id(namespace, path)))
    }

    @Throws(IllegalArgumentException::class)
    private fun parseIdentifierRule(notation: String): GroupingRule.Identifier {
        val (tokenType, namespace, path) = splitParts(notation)
        val token = parseToken(tokenType.trimStart('&'))
        return GroupingRule.Identifier(token, id(namespace, path))
    }

    @Throws(IllegalArgumentException::class, NoSuchElementException::class)
    private fun parseStackRule(notation: String): GroupingRule.Stack {
        val (tokenType, namespace, path) = splitParts(notation)
        var token = parseToken(tokenType)
        if (token == RegistryTokens.BLOCK) token = RegistryTokens.ITEM

        // TODO: fast deserialization
        val serializer = EmiIngredientSerializers.BY_TYPE[token.serializationType]
        val stack = serializer?.deserialize(JsonPrimitive("${token.serializationType}:$namespace:$path"))

        stack as? EmiStack ?: throw IllegalArgumentException("Invalid stack: $notation")
        return GroupingRule.Stack(token, stack)
    }

    @Throws(IllegalArgumentException::class)
    private fun parseRegexRule(notation: String): GroupingRule.Regex {
        val parts = notation.split(':')
        if (parts.size != 2) throw IllegalArgumentException("Invalid Stack format: $notation")
        val (tokenType, regexPattern) = parts
        val token = parseToken(tokenType)
        return GroupingRule.Regex(token, regexPattern.toRegex())
    }

}