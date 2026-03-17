package concerrox.emixx.content.stackgroup.editor.rule

import concerrox.blueberry.ui.binding.ViewModel
import concerrox.emixx.Minecraft
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.content.stackgroup.data.RegistryToken
import concerrox.emixx.content.stackgroup.data.RegistryTokens
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import net.minecraft.core.HolderSet
import net.minecraft.network.chat.Component
import net.minecraft.tags.TagKey
import kotlin.jvm.optionals.getOrNull

data class TagUiState(
    val rule: GroupingRule.Tag,
    val key: TagKey<*>,
    val name: Component,
    val content: List<EmiIngredient>
)
//data class TagPickerUiState(val tags: List<TagUiState>, val selected: TagUiState?)
//data class IdentifierPickerUiState(val stacks: List<EmiIngredient>)
//data class StackPickerUiState(val stacks: List<EmiIngredient>)

sealed class PickerUiState {
    data class Tag(val tags: List<TagUiState> = emptyList()) : PickerUiState()
    data class Identifier(val stacks: List<EmiIngredient>) : PickerUiState()
    data class Stack(val stacks: List<EmiIngredient>) : PickerUiState()
    data class Regex(val stacks: List<EmiIngredient>) : PickerUiState()
}

data class GroupingRuleDialogUiState(
    val pickerUiState: PickerUiState = PickerUiState.Tag(),
    val selected: GroupingRule? = null,
    val notation: String = "",
    val previewStacks: List<EmiIngredient> = emptyList(),
)

class GroupingRuleDialogViewModel(private var rule: GroupingRule?) : ViewModel() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val registryToken = MutableStateFlow(rule?.registryToken ?: RegistryTokens.ITEM)
    private val ruleType = MutableStateFlow(rule?.type ?: GroupingRule.Type.TAG)
    private val searchKeyword = MutableStateFlow("")
    val selected = MutableStateFlow(rule)

    val uiState = combine(registryToken, ruleType, searchKeyword, selected) { token, type, keyword, selected ->
        val pickerUiState = when (type) {
            GroupingRule.Type.TAG -> PickerUiState.Tag(queryTags(token, keyword))
            GroupingRule.Type.IDENTIFIER -> PickerUiState.Identifier(rule?.loadContent() ?: emptyList())
            GroupingRule.Type.STACK -> PickerUiState.Stack(rule?.loadContent() ?: emptyList())
            GroupingRule.Type.REGEX -> PickerUiState.Regex(rule?.loadContent() ?: emptyList())
        }
        GroupingRuleDialogUiState(
            pickerUiState, selected, selected?.encode() ?: "", selected?.loadContent() ?: emptyList()
        )
    }.flowOn(Dispatchers.Default).stateIn(
        coroutineScope, SharingStarted.Eagerly, GroupingRuleDialogUiState()
    )

//    var registryToken = rule?.registryToken ?: RegistryTokens.ITEM
//    var ruleType = rule?.type ?: GroupingRule.Type.TAG

//    val notation = liveData { "" }
//    val previewStacks = liveData { emptyList<EmiStack>() }

    // Picker UI states
//    val tagPickerUiState = liveData { TagPickerUiState(emptyList(), null) }
//    val identifierPickerUiState = liveData { }
//    val stackPickerUiState = liveData { }
//    val regexPickerUiState = liveData { }

    internal fun updateRegistryToken(token: RegistryToken<out Any?, out EmiStack>) {
        searchKeyword.value = ""
        registryToken.value = token
    }

    internal fun updateRuleType(type: GroupingRule.Type) {
        searchKeyword.value = ""
        ruleType.value = type
    }

    internal fun select(uiState: TagUiState) {
        selected.value = uiState.rule
    }

//    private fun updatePickerUiState() {
//        when (ruleType) {
//            GroupingRule.Type.TAG -> updateTagPickerUiState()
//            else -> {}
//        }
//    }

//    private fun updateTagPickerUiState(keyword: String = "") {
//        val rule = rule as GroupingRule.Tag
//        tagPickerUiState.value = TagPickerUiState(
//            queryTags(keyword), TagUiState(rule.tag, EmiTags.getTagName(rule.tag), rule.loadContent())
//        )
//    }

    private fun queryTags(token: RegistryToken<out Any?, out EmiStack>, keyword: String = ""): List<TagUiState> {
        return token.registry.tags.map { pair ->
            val tagKey = pair.first
            val holderSet = pair.second
            val rule = GroupingRule.Tag(token, tagKey)
            TagUiState(rule, tagKey, EmiTags.getTagName(tagKey), collectTagContent(token, tagKey, holderSet))
        }.filter {
            it.key.location.toString().contains(keyword) || it.name.string.contains(keyword)
        }
//            .filter {
//            it.content.size != 1 // TODO: fix this
//        }
            .toList()
    }

    @Suppress("unchecked_cast")
    fun collectTagContent(
        token: RegistryToken<out Any?, out EmiStack>,
        tag: TagKey<*>,
        holderSet: HolderSet.Named<out Any>
    ): List<EmiStack> {
        val access = Minecraft.level?.registryAccess() ?: throw IllegalStateException("No registry access")
        val registry = access.registry(tag.registry).orElseThrow()

        val ret = mutableListOf<EmiStack>()
        for (stack in EmiStackList.stacks) {
            if (!token.isIn(stack)) continue

            val holder = registry.getHolder(stack.id).getOrNull() ?: continue
            if (!holder.`is`(tag as TagKey<Any>)) continue
            ret += stack
        }
        return ret
    }

    /* All the below is deprecated */

    //    var availableTags = emptyMap<String, TagKey<*>>() // Tag ID to TagKey
//    val selectedTag = liveData { (rule as? GroupingRule.Tag?)?.tag }
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


//    val availableStacks = liveData<List<EmiIngredient>> { EmiStackList.stacks }
//    val matchDataComponents = liveData { false }
//    var pickedStack = liveData<EmiIngredient?> { null }

    init {
//        registryToken.observe { updateToken(it) }
//        matchDataComponents.observe {
//            if (ruleType == GroupingRule.Type.STACK || ruleType == GroupingRule.Type.IDENTIFIER) {
//                ruleType = if (it) {
//                    GroupingRule.Type.STACK
//                } else {
//                    GroupingRule.Type.IDENTIFIER
//                }
//            }
//        }
//        selectedTag.observe { updateRule() }
//        pickedStack.observe { updateRule() }
//        searchKeyword.observe { value ->
//            EmiPlusPlusSearch.search(EmiStackList.stacks, value) {
//                availableStacks.value = it
//            }
//        }
    }


//    fun updateType(typeNameIndex: Int) {
//        ruleType = GroupingRule.Type.entries[if (typeNameIndex >= 1) typeNameIndex + 1 else 0]
//        updateRule()
//    }
//
//    fun updateToken(token: RegistryToken<out Any?, out EmiStack>) {
//        selectedTag.value = null
//        availableTags = if (token == RegistryTokens.BLOCK) {
//            token.registry.tagNames.toList().associateBy { it.location.toString() }
//        } else {
//            EmiTags.getTags(token.registry).associateBy { it.location.toString() }
//        }
//    }

//    fun updateRule() {
//        rule = when (type) {
//            GroupingRule.Type.TAG -> {
//                val tag = selectedTag.value
//                if (tag != null) GroupingRule.Tag(registryToken.value, tag) else null
//            }
//
//            GroupingRule.Type.IDENTIFIER -> {
//                val stack = pickedStack.value
//                null//if (stack != null) GroupingRule.Identifier(token, stack) else null
//            }
//
//            else -> null
//        }
//
//        notation.value = rule?.encode() ?: ""
//        previewStacks.value = rule?.loadContent() ?: emptyList()
//    }

}