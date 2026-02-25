package concerrox.emixx.content.stackgroup.editor

import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler
import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.content.stackgroup.data.RegistryTokens
import concerrox.emixx.content.stackgroup.search.EmiPlusPlusSearch
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import net.minecraft.tags.TagKey

class StackGroupRuleViewModel(private val editingRule: GroupingRule? = null) : ViewModel() {

    var rule = editingRule
    private var type = GroupingRule.Type.TAG
    private val token = liveData { RegistryTokens.getBySerializationType("tag") }

    val tokenSerializationTypes = RegistryTokens.listTokens().map { it.serializationType }
    val tokenSerializationType = liveData { token.value?.serializationType ?: "item" }

    val searchKeyword = liveData { "" }

    val notation = liveData { editingRule?.encode() ?: "" }
    val previewStacks = liveData<List<EmiIngredient>> {
        when (editingRule) {
            is GroupingRule.Tag -> EmiTags.getValues(editingRule.tag)
//            is GroupingRule.Regex -> EmiTags.getValues(currentRule.tag)
//            is GroupingRule.Stack -> listOf(currentRule.stack)
            else -> emptyList()
        }
    }

    var availableTags = emptyMap<String, TagKey<*>>() // Tag ID to TagKey
    val selectedTag = liveData { (editingRule as? GroupingRule.Tag?)?.tag }
    val tagSearcherById = object : SearchComponent.ISearchUI<TagKey<*>?> {
        override fun resultText(value: TagKey<*>?) = value?.location.toString()
        override fun onResultSelected(value: TagKey<*>?) {}
        override fun search(keyword: String, searchHandler: IResultHandler<TagKey<*>?>) {
            if (keyword.isBlank()) return
            availableTags.forEach { (id, key) ->
                if (id.contains(keyword)) searchHandler.accept(key)
            }
        }
    }


    val availableStacks = liveData<List<EmiIngredient>> { EmiStackList.stacks }
    val matchDataComponents = liveData { false }
    var pickedStack = liveData<EmiIngredient?> { null }

    init {
        tokenSerializationType.observe { updateToken(it) }
        matchDataComponents.observe {
            if (type == GroupingRule.Type.STACK || type == GroupingRule.Type.ID) {
                type = if (it) {
                    GroupingRule.Type.STACK
                } else {
                    GroupingRule.Type.ID
                }
            }
        }
        selectedTag.observe { updateRule() }
        pickedStack.observe { updateRule() }
        searchKeyword.observe { value ->
            EmiPlusPlusSearch.search(EmiStackList.stacks, value) {
                availableStacks.value = it
            }
        }
    }

    fun updateType(typeNameIndex: Int) {
        type = GroupingRule.Type.entries[if (typeNameIndex >= 1) typeNameIndex + 1 else 0]
        updateRule()
    }

    fun updateToken(tokenType: String) {
        val new = RegistryTokens.getBySerializationType(tokenType) ?: return
        token.value = new
        selectedTag.value = null
        availableTags = if (tokenType == "block") {
            new.registry.tagNames.toList().associateBy { it.location.toString() }
        } else {
            EmiTags.getTags(new.registry).associateBy { it.location.toString() }
        }
    }

    fun updateRule() {
        val token = RegistryTokens.getBySerializationType(tokenSerializationType.value) ?: return
        rule = when (type) {
            GroupingRule.Type.TAG -> {
                val tag = selectedTag.value
                if (tag != null) GroupingRule.Tag(token, tag) else null
            }
            GroupingRule.Type.ID -> {
                val stack = pickedStack.value
                if (stack != null) GroupingRule.Identifier(token, stack) else null
            }
            else -> null
        }

        notation.value = rule?.encode() ?: ""
        previewStacks.value = rule?.loadContent() ?: emptyList()
    }

}