package concerrox.emixx.content.stackgroup.editor

import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler
import com.mojang.serialization.JsonOps
import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.content.stackgroup.data.EmiPlusPlusRegistryTokens
import concerrox.emixx.content.stackgroup.data.GroupingRule
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import net.minecraft.tags.TagKey

private typealias RegistryTokens = EmiPlusPlusRegistryTokens

class StackGroupRuleViewModel(private val editingRule: GroupingRule? = null) : ViewModel() {

    var rule = editingRule
    private var type = GroupingRule.Type.TAG
    private val token = liveData { RegistryTokens.getBySerializationType("tag") }

    val tokenSerializationTypes = RegistryTokens.listTokens().map { it.serializationType }
    val tokenSerializationType = liveData { token.value?.serializationType ?: "item" }

    val searchKeyword = liveData { "" }

    val notation = liveData { encodeRule(editingRule) ?: "" }
    val previewStacks = liveData {
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


    val availableStacks = liveData { EmiStackList.stacks }

    // ============================ Regex =============================

    // ============================ Stack =============================

    init {
        tokenSerializationType.observe { updateToken(it) }
        selectedTag.observe { updateRule() }
        searchKeyword.observe { availableStacks.value = EmiStackList.stacks }
    }

    fun updateType(typeNameIndex: Int) {
        type = GroupingRule.Type.entries[typeNameIndex]
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
        rule = when (type) {
            GroupingRule.Type.TAG -> {
                val tag = selectedTag.value
                if (tag != null) GroupingRule.Tag(tag) else null
            }

            else -> null
        }

        notation.value = encodeRule(rule) ?: ""
        previewStacks.value = rule?.loadContent() ?: emptyList()
    }

    private fun encodeRule(rule: GroupingRule?): String? {
        if (rule == null) return null
        return GroupingRule.CODEC.encodeStart(JsonOps.INSTANCE, rule).getOrThrow().asString
    }

}