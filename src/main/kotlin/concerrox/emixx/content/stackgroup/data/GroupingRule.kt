package concerrox.emixx.content.stackgroup.data

import concerrox.blueberry.registry.TranslationKey
import concerrox.emixx.registry.ModLang
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiIngredientSerializers
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import net.minecraft.tags.TagKey

sealed class GroupingRule(val type: Type, val registryToken: RegistryToken<*, *>) {

    enum class Type(val nameKey: TranslationKey, val descriptionKey: TranslationKey) {
        TAG(ModLang.tag, ModLang.tagDesc),
        IDENTIFIER(ModLang.identifier, ModLang.identifierDesc),
        STACK(ModLang.stack, ModLang.stackDesc),
        REGEX(ModLang.regex, ModLang.regexDesc);
    }

    internal val typeName = registryToken.serializationType
    abstract fun match(stack: EmiStack): Boolean
    abstract fun encode(): String

    @Deprecated("")
    fun loadContent(): List<EmiStack> {
        val ret = mutableListOf<EmiStack>()
        for (stack in EmiStackList.filteredStacks) if (match(stack)) ret += stack
        return ret
    }

    class Tag(registryToken: RegistryToken<*, *>, val tag: TagKey<*>) : GroupingRule(Type.TAG, registryToken) {
        @Deprecated("")
        private val tagContent = EmiTags.getRawValues(tag).toSet() // TODO: rewrite this
        override fun match(stack: EmiStack) = stack in tagContent
        override fun encode() = "#$typeName:${tag.location}"
    }

    class Identifier(registryToken: RegistryToken<*, *>, val id: concerrox.emixx.Identifier) :
        GroupingRule(Type.IDENTIFIER, registryToken) {
        override fun match(stack: EmiStack) = stack.id == id // TODO: rewrite this
        override fun encode() = "&$typeName:$id"
    }

    class Stack(registryToken: RegistryToken<*, *>, val stack: EmiStack) : GroupingRule(Type.STACK, registryToken) {
        override fun match(stack: EmiStack) = stack.id == this.stack.id && stack == this.stack
        override fun encode() = "&$typeName:${EmiIngredientSerializers.serialize(stack).toString().trim('"')}"
    }

    class Regex(registryToken: RegistryToken<*, *>, val expression: kotlin.text.Regex) :
        GroupingRule(Type.REGEX, registryToken) {
        override fun match(stack: EmiStack) = expression.matches(stack.id.toString())
        override fun encode(): String = "&$typeName:/${expression.pattern}/"
    }

}