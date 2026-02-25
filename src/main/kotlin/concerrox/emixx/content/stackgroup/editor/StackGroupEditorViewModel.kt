package concerrox.emixx.content.stackgroup.editor

import com.ibm.icu.text.Transliterator
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture
import com.mojang.serialization.JsonOps
import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.Identifier
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.data.GroupingRule
import concerrox.emixx.registry.ModSprites
import concerrox.emixx.registry.ModTranslationKeys
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class StackGroupEditorViewModel(val editingStackGroup: EmiStackGroupV2? = null) : ViewModel() {

    companion object {
        private val transliterator = Transliterator.getInstance("Any-Latin;Latin-ASCII;")
    }

    val stackGroupId = liveData { editingStackGroup?.id?.toString() ?: "" }
    val stackGroupName = liveData { editingStackGroup?.name ?: "New" }
    val stackGroupEnabled = liveData { editingStackGroup?.isEnabled ?: true }
    val stackGroupRules = liveData { editingStackGroup?.rules ?: emptyList() }
    val stackGroupRuleUiStates = liveData { stackGroupRules.value.map(::RuleUiState) }
    val previewStacks = liveData<List<EmiIngredient>> { editingStackGroup?.loadContent() ?: emptyList() }

    init {
        stackGroupName.observe {
            val path = transliterator.transliterate(it).lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_.-]"), "")
            stackGroupId.value = "${EmiPlusPlus.MOD_ID}:custom/$path"
        }
        stackGroupRules.observe {
            previewStacks.value = EmiStackGroupV2(
                Identifier.parse(stackGroupId.value), stackGroupName.value, stackGroupEnabled.value, it
            ).loadContent()
        }
    }

    fun addRule(rule: GroupingRule) {
        stackGroupRules.value += rule
        stackGroupRuleUiStates.value += RuleUiState(rule)
    }

    fun removeRule(rule: GroupingRule) {
        stackGroupRules.value -= rule
        stackGroupRuleUiStates.value = stackGroupRuleUiStates.value.filter { it.rule != rule }
    }

    fun updateRule(rule: GroupingRule) {
        val rules = stackGroupRules.value.toMutableList()
        val idx = rules.indexOf(rule)
        rules[idx] = rule
        stackGroupRules.value = rules

        val uiStates = stackGroupRuleUiStates.value.toMutableList()
        uiStates[idx] = RuleUiState(rule)
        stackGroupRuleUiStates.value = uiStates
    }

    data class RuleUiState(
        val rule: GroupingRule,
        val icon: SpriteTexture,
        val titleComponent: Component,
        val notationComponent: Component,
        val previewStacks: List<EmiStack>
    ) {

        constructor(rule: GroupingRule) : this(
            rule = rule,
            icon = when (rule) {
                is GroupingRule.Tag -> ModSprites.ICON_GROUPING_RULE_TAG
                is GroupingRule.Identifier -> ModSprites.ICON_GROUPING_RULE_TAG
                is GroupingRule.Stack -> ModSprites.ICON_GROUPING_RULE_STACK
                is GroupingRule.Regex -> ModSprites.ICON_GROUPING_RULE_REGEX
            },
            titleComponent = when (rule) {
                is GroupingRule.Tag -> ModTranslationKeys.Config.STACK_GROUP_CONFIG_GROUPING_RULE_TAG_TITLE.asComponent(
                    "Item", Component.literal(rule.tag.location.toString()).withStyle(ChatFormatting.GOLD)
                )

                is GroupingRule.Identifier -> Component.translatable("stackgroup.rule.stack.title")
                is GroupingRule.Stack -> Component.translatable("stackgroup.rule.stack.title")
                is GroupingRule.Regex -> Component.translatable("stackgroup.rule.regex.title")
            },
            notationComponent = Component.literal(
                GroupingRule.CODEC.encodeStart(JsonOps.INSTANCE, rule).getOrThrow().asString
            ),
            previewStacks = rule.loadContent(),
        )

    }

}