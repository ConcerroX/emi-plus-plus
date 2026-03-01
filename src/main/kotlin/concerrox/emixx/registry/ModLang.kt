package concerrox.emixx.registry

import concerrox.blueberry.registry.LangRegistry
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig

object ModLang : LangRegistry(EmiPlusPlus.MOD_ID) {

    val ui = root["ui"]
    val save   by ui.define("Save")
    val cancel by ui.define("Cancel")

    val stackGroup = ui["stackGroup"]
    val editGroupingRule   by stackGroup.define("Edit Grouping Rule")
    val stackType          by stackGroup.define("Stack Type")
    val ruleNotation       by stackGroup.define("Rule Notation")
    val matchedStacks      by stackGroup.define("Matched Stacks")
    val stackGroupName     by stackGroup.define("Stack Group Name")
    val stackGroupId       by stackGroup.define("Stack Group ID")
    val stackGroupFilename by stackGroup.define("Stack Group Filename")

    val groupingRuleType = stackGroup["groupingRuleType"]
    val ruleType       by groupingRuleType.define("Rule Type")
    val tag            by groupingRuleType.define("Tag")
    val tagDesc        = defineDescription(tag, "Match stacks with the specified tag")
    val identifier     by groupingRuleType.define("ID")
    val identifierDesc = defineDescription(identifier, "Match stacks with the specified ID (e.g., minecraft:enchanted_book) ")
    val stack          by groupingRuleType.define("Stack")
    val stackDesc      = defineDescription(stack, "Match stacks with ID and data components (e.g., tank with a specified fluid) ")
    val regex          by groupingRuleType.define("Regex")
    val regexDesc      = defineDescription(regex, "Match stacks using the regular expression on stack IDs (e.g., spawn eggs) ")

    val registryToken = root["registryToken"]
    val item     by registryToken["minecraft"].define("Item")
    val block    by registryToken["minecraft"].define("Block")
    val fluid    by registryToken["minecraft"].define("Fluid")
    val chemical by registryToken["mekanism" ].define("Chemical")

    init {
        config.define(EmiPlusPlusConfig.indexSidebarHeaderWidget, "Index Sidebar Header Widget")
        config.define(EmiPlusPlusConfig.indexSidebarFooterWidget, "Index Sidebar Footer Widget")
        config.define(EmiPlusPlusConfig.indexSidebarTabButtonSize, "Index Sidebar Tab Button Size")
        config.define(EmiPlusPlusConfig.hidePaginationButtons, "Hide Pagination Buttons")
    }

}