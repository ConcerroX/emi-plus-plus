package concerrox.emixx.content.stackgroup.editor.rule

import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.content.stackgroup.data.RegistryToken
import concerrox.emixx.content.stackgroup.data.RegistryTokens
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.chat.Component
import net.minecraft.tags.TagKey

class GroupingRuleDialogViewModel(private val editingRule: GroupingRule? = null) : ViewModel() {

    var rule = editingRule
    private var type = GroupingRule.Type.TAG

    // Shared input & results
    val registryToken = liveData { editingRule?.registryToken ?: RegistryTokens.ITEM }
    val ruleType = liveData { type }

    //    val searchKeyword = liveData { "" }
    val notation = liveData { editingRule?.encode() ?: "" }
    val previewStacks = liveData<List<EmiIngredient>> {
        when (editingRule) {
            is GroupingRule.Tag -> EmiTags.getValues(editingRule.tag)
            is GroupingRule.Identifier -> EmiStackList.stacks.filter { it.id == (editingRule.id) }
//            is GroupingRule.Regex -> EmiTags.getValues(currentRule.tag)
//            is GroupingRule.Stack -> listOf(currentRule.stack)
            else -> emptyList()
        }
    }

    // Picker states
    val tagPickerUiState = liveData {
        val rule = rule
        if (rule is GroupingRule.Tag) {
            TagPickerUiState(queryTags(), TagUiState(rule.tag, EmiTags.getTagName(rule.tag), rule.loadContent()))
        } else {
            TagPickerUiState(queryTags(), null)
        }
    }
    val identifierPickerUiState = liveData { }
    val stackPickerUiState = liveData { }
    val regexPickerUiState = liveData { }

    private fun queryTags(keyword: String = ""): List<TagUiState> {
        val token = registryToken.value
        return token.registry.tags.map { pair ->
            val tagKey = pair.first
            val holderSet = pair.second
            TagUiState(tagKey, EmiTags.getTagName(tagKey), holderSet.map { holder ->
                token.stackSerializer.create(holder.key!!.location(), DataComponentPatch.EMPTY, 1)
            })
        }.filter {
            it.key.location.toString().contains(keyword) || it.name.string.contains(keyword)
        }.filter {
            it.content.size != 1
        }.toList()
    }

    data class TagUiState(val key: TagKey<*>, val name: Component, val content: List<EmiIngredient>)
    data class TagPickerUiState(val tags: List<TagUiState>, val selected: TagUiState?)
    data class IdentifierPickerUiState(val stacks: List<EmiIngredient>)
    data class StackPickerUiState(val stacks: List<EmiIngredient>)
//    data class RegexPickerUiState(val regex: String)


    /* All the below is deprecated */

    var availableTags = emptyMap<String, TagKey<*>>() // Tag ID to TagKey
    val selectedTag = liveData { (editingRule as? GroupingRule.Tag?)?.tag }
//    val tagSearcherById = object : SearchComponent.ISearchUI<TagKey<*>?> {
//        override fun resultText(value: TagKey<*>?) = value?.location.toString()
//        override fun onResultSelected(value: TagKey<*>?) {}
//        override fun search(keyword: String, searchHandler: IResultHandler<TagKey<*>?>) {
//            if (keyword.isBlank()) return
//            availableTags.forEach { (id, key) ->
//                if (id.contains(keyword)) searchHandler.accept(key)
//            }
//        }
//    }


    val availableStacks = liveData<List<EmiIngredient>> { EmiStackList.stacks }
    val matchDataComponents = liveData { false }
    var pickedStack = liveData<EmiIngredient?> { null }

    init {
        registryToken.observe { updateToken(it) }
        matchDataComponents.observe {
            if (type == GroupingRule.Type.STACK || type == GroupingRule.Type.IDENTIFIER) {
                type = if (it) {
                    GroupingRule.Type.STACK
                } else {
                    GroupingRule.Type.IDENTIFIER
                }
            }
        }
        selectedTag.observe { updateRule() }
        pickedStack.observe { updateRule() }
//        searchKeyword.observe { value ->
//            EmiPlusPlusSearch.search(EmiStackList.stacks, value) {
//                availableStacks.value = it
//            }
//        }
    }


    fun updateType(typeNameIndex: Int) {
        type = GroupingRule.Type.entries[if (typeNameIndex >= 1) typeNameIndex + 1 else 0]
        updateRule()
    }

    fun updateToken(token: RegistryToken<out Any?, out EmiStack>) {
        selectedTag.value = null
        availableTags = if (token == RegistryTokens.BLOCK) {
            token.registry.tagNames.toList().associateBy { it.location.toString() }
        } else {
            EmiTags.getTags(token.registry).associateBy { it.location.toString() }
        }
    }

    fun updateRule() {
        rule = when (type) {
            GroupingRule.Type.TAG -> {
                val tag = selectedTag.value
                if (tag != null) GroupingRule.Tag(registryToken.value, tag) else null
            }

            GroupingRule.Type.IDENTIFIER -> {
                val stack = pickedStack.value
                null//if (stack != null) GroupingRule.Identifier(token, stack) else null
            }

            else -> null
        }

        notation.value = rule?.encode() ?: ""
        previewStacks.value = rule?.loadContent() ?: emptyList()
    }

}