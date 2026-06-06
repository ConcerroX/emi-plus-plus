package concerrox.minecraft.emiplusplus.editor

import com.google.gson.JsonPrimitive
import concerrox.minecraft.emiplusplus.group.EmiGroupStack
import concerrox.minecraft.emiplusplus.group.GroupConfig
import concerrox.minecraft.emiplusplus.group.GroupSelector
import concerrox.minecraft.emiplusplus.group.StackGroups
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.world.item.Item
import net.minecraft.world.level.material.Fluid

internal fun StackGroupEditorScreen.createIngredient(selector: String): EmiIngredient {
    return if (selector.startsWith("#")) {
        EmiIngredientSerializer.getDeserialized(JsonPrimitive(selector))
    } else {
        val stacks = getPreviewStacks(selector)
        if (stacks.isEmpty()) EmiStack.EMPTY else EmiIngredient.of(stacks)
    }
}

internal fun getPreviewStacks(notation: String): List<EmiStack> {
    val parsed = GroupSelector.parse(notation) ?: return emptyList()
    return EmiStackList.stacks.filter { parsed.match(it) }.take(50)
}

internal fun StackGroupEditorScreen.updateGroup(group: GroupConfig, includes: List<String>) {
    val idx = StackGroups.groups.indexOfFirst { it.id == group.id }
    if (idx >= 0) StackGroups.groups[idx] = group.copy(includes = includes)
    StackGroups.saveAll()
    StackGroups.reload()
    StackGroups.expandById(group.id)
    bakePages()
    rebuildEditor()
}

internal fun StackGroupEditorScreen.deleteGroup(group: GroupConfig) {
    java.nio.file.Files.deleteIfExists(
        StackGroups.groupsDir().resolve(group.id.replace(":", "__").replace("/", "__") + ".json")
    )
    StackGroups.groups.removeAll { it.id == group.id }
    StackGroups.bakeOnly()
    selectedGroupId = null
    editMode = EditMode.NONE
    bakePages()
    if (currentPage >= pages.size) currentPage = maxOf(0, pages.size - 1)
    rebuildEditor()
}

internal fun StackGroupEditorScreen.createNewGroup() {
    StackGroups.groups.add(GroupConfig(
        name = "New Group",
        id = "emixx:custom_${System.currentTimeMillis() % 100000}",
        includes = emptyList()
    ))
    StackGroups.saveAll()
    StackGroups.reload()
    val newId = StackGroups.groups.last().id
    bakePages()
    currentPage = pages.indexOfFirst { it.any { g -> g.id == newId } }.coerceAtLeast(0)
    selectedGroupId = newId
    StackGroups.expandById(newId)
    rebuildEditor()
}

internal fun StackGroupEditorScreen.handleAddModeClick(mouseX: Double, mouseY: Double) {
    val ingredient = EmiScreenManager.getHoveredStack(mouseX.toInt(), mouseY.toInt(), false).stack
    if (ingredient !is EmiStack || ingredient.isEmpty) return
    if (ingredient is EmiGroupStack) return
    when (editMode) {
        is EditMode.AddById -> addById((editMode as EditMode.AddById).groupId, ingredient)
        is EditMode.AddByTag -> addByTag((editMode as EditMode.AddByTag).groupId, ingredient)
        else -> {}
    }
}

internal fun StackGroupEditorScreen.addById(groupId: String, stack: EmiStack) {
    val group = StackGroups.groups.find { it.id == groupId } ?: return
    val notation = registryType(stack) + ":" + stack.id
    if (group.includes.contains(notation)) return
    updateGroup(group, group.includes + notation)
}

internal fun StackGroupEditorScreen.addByTag(groupId: String, stack: EmiStack) {
    val group = StackGroups.groups.find { it.id == groupId } ?: return
    val availableTags = getTagsForStack(stack)
    if (availableTags.isEmpty()) return
    val w = minOf(260, width - 60)
    val h = minOf(availableTags.size * 18 + 8, height - 40)
    tagOverlay = TagSelectionOverlay((width - w) / 2, (height - h) / 2, w, h, availableTags) { selected ->
        tagOverlay = null
        if (selected.first.isNotEmpty() && !group.includes.contains(selected.first)) {
            updateGroup(group, group.includes + selected.first)
        }
    }
}

private fun registryType(stack: EmiStack): String = when (stack.key) {
    is Item -> "item"
    is Fluid -> "fluid"
    else -> "item"
}

internal fun getTagsForStack(stack: EmiStack): List<Pair<String, String>> {
    val results = mutableListOf<Pair<String, String>>()
    try {
        val key = stack.key
        val adapter = EmiTags.ADAPTERS_BY_CLASS[key::class.java] ?: return results
        for (tagKey in EmiTags.getTags(adapter.registry)) {
            if (tagKey.set.contains(key)) {
                val prefix = when (adapter.registry.key().location().toString()) {
                    "minecraft:item" -> "item"
                    "minecraft:fluid" -> "fluid"
                    else -> adapter.registry.key().location().path
                }
                val notation = "#$prefix:${tagKey.id()}"
                if (results.none { it.first == notation }) results.add(notation to tagKey.id().toString())
            }
        }
    } catch (_: Exception) {}
    return results
}
