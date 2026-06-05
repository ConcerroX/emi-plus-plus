package concerrox.minecraft.emiplusplus.editor

import com.google.gson.JsonPrimitive
import concerrox.minecraft.emiplusplus.group.GroupConfig
import concerrox.minecraft.emiplusplus.group.StackGroups
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.registry.EmiTags
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.screen.widget.SizedButtonWidget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StackGroupEditorScreen : Screen(Component.literal("EMI++ Group Editor")) {

    companion object {
        private val TEXTURE = EmiPort.id("emi", "textures/gui/background.png")
        private const val ROW_HEIGHT = 12
    }

    var editMode: EditMode = EditMode.NONE
    private var tagOverlay: TagSelectionOverlay? = null

    private var backgroundWidth = 176
    private var backgroundHeight = 200
    private var panelX = 0
    private var panelY = 0
    private var currentPage = 0
    private var groupsPerPage = 1

    override fun init() {
        super.init()

        backgroundWidth = 176
        backgroundHeight = minOf(height - 40, 220)
        panelX = (width - backgroundWidth) / 2
        panelY = (height - backgroundHeight) / 2 + 1

        EmiScreenManager.addWidgets(this)
        rebuildEditor()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderTransparentBackground(graphics)

        val emiContext = EmiDrawContext.wrap(graphics)
        emiContext.resetColor()

        // Main panel background
        EmiRenderHelper.drawNinePatch(
            emiContext, TEXTURE, panelX, panelY, backgroundWidth, backgroundHeight,
            0, 0, 4, 1
        )

        // Page indicator bar
        EmiRenderHelper.drawNinePatch(
            emiContext, TEXTURE,
            panelX + 19, panelY + 5, backgroundWidth - 38, 12,
            0, 16, 3, 6
        )
        val totalPages = maxOf(1, (StackGroups.groups.size + groupsPerPage - 1) / groupsPerPage)
        emiContext.drawCenteredTextWithShadow(
            EmiRenderHelper.getPageText(currentPage + 1, totalPages, backgroundWidth - 40),
            panelX + backgroundWidth / 2, panelY + 7, 0xFFFFFF
        )

        // Content: group cards with 9-patch backgrounds
        val startIndex = currentPage * groupsPerPage
        val visibleGroups = StackGroups.groups.drop(startIndex).take(groupsPerPage)
        var cardY = panelY + 19
        val cardLeft = panelX + 5
        val cardWidth = backgroundWidth - 10

        for (group in visibleGroups) {
            val selectorCount = group.includes.size
            val cardHeight = 28 + 4 + selectorCount * 18 + 18

            if (cardY + cardHeight > panelY + backgroundHeight - 26) break

            EmiRenderHelper.drawNinePatch(
                emiContext, TEXTURE,
                cardLeft, cardY, cardWidth, cardHeight,
                27, 0, 4, 1
            )

            var lineY = cardY + 4
            graphics.drawString(font, group.name, cardLeft + 4, lineY, 0x000000, false)
            lineY += 10
            graphics.drawString(font, group.id, cardLeft + 4, lineY, 0x404040, false)
            lineY += 12 // font height + gap before slots

            for (selector in group.includes) {
                val ingredient = createIngredient(selector)
                val slotX = cardLeft + 4
                val slotY = lineY

                val slot = SlotWidget(ingredient, slotX, slotY)
                slot.render(graphics, mouseX, mouseY, 0f)

                // EMI's native tooltip rendering via SlotWidget
                if (!ingredient.isEmpty && mouseX in slotX..slotX + 18 && mouseY in slotY..slotY + 18) {
                    val tooltip = slot.getTooltip(mouseX, mouseY)
                    if (tooltip.isNotEmpty()) {
                        EmiRenderHelper.drawTooltip(this, emiContext, tooltip, mouseX, mouseY)
                    }
                }

                graphics.drawString(font, selector, slotX + 22, slotY + 5, 0x404040, false)
                lineY += 18
            }

            if (editMode != EditMode.NONE) {
                val eid = (editMode as? EditMode.AddById)?.groupId
                    ?: (editMode as? EditMode.AddByTag)?.groupId ?: ""
                if (eid == group.id) {
                    graphics.drawString(font, "Add mode active", cardLeft + 4, lineY, 0x00AA00, false)
                    lineY += ROW_HEIGHT
                }
            }

            cardY += cardHeight + 2
        }

        // EMI overlay
        EmiScreenManager.drawBackground(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.render(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.drawForeground(emiContext, mouseX, mouseY, delta)

        tagOverlay?.render(graphics, mouseX, mouseY)
        super.render(graphics, mouseX, mouseY, delta)
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Handled in render()
    }

    private fun rebuildEditor() {
        clearWidgets()

        val totalGroups = StackGroups.groups.size

        // Compute groups per page dynamically
        groupsPerPage = calculateGroupsPerPage()
        val totalPages = maxOf(1, totalGroups)
        val startIndex = currentPage * groupsPerPage
        val visibleGroups = StackGroups.groups.drop(startIndex).take(groupsPerPage)

        // Page arrows (RecipeScreen-style)
        val pageActive = totalPages > 1
        if (pageActive) {
            addRenderableWidget(
                SizedButtonWidget(panelX + 5, panelY + 5, 12, 12, 0, 0,
                    { true },
                    { if (currentPage > 0) { currentPage--; rebuildEditor() } else { currentPage = totalPages - 1; rebuildEditor() } }
                )
            )
            addRenderableWidget(
                SizedButtonWidget(panelX + backgroundWidth - 17, panelY + 5, 12, 12, 12, 0,
                    { true },
                    { currentPage = (currentPage + 1) % totalPages; rebuildEditor() }
                )
            )
        }

        if (totalGroups == 0) {
            addRenderableWidget(
                Button.builder(Component.literal("+ New Group")) { createNewGroup() }
                    .bounds(panelX + 4, panelY + backgroundHeight - 22, backgroundWidth - 8, 18)
                    .build()
            )
            return
        }

        val cardLeft = panelX + 5
        val cardWidth = backgroundWidth - 10
        var cardY = panelY + 19

        for (group in visibleGroups) {
            val selectorCount = group.includes.size
            val cardHeight = 28 + 4 + selectorCount * 18 + 18

            var lineY = cardY + 28 + 4

            for (selector in group.includes) {
                addRenderableWidget(
                    Button.builder(Component.literal("✕")) {
                        updateGroup(group, group.includes - selector)
                    }
                    .size(14, 12)
                    .bounds(panelX + backgroundWidth - 24, lineY, 14, 12)
                    .build()
                )
                lineY += ROW_HEIGHT + 1
            }

            // Action row
            val actionRowY = cardY + cardHeight - 16
            val inAddMode = (editMode is EditMode.AddById && (editMode as EditMode.AddById).groupId == group.id) ||
                (editMode is EditMode.AddByTag && (editMode as EditMode.AddByTag).groupId == group.id)

            if (inAddMode) {
                addRenderableWidget(
                    Button.builder(Component.literal("Cancel")) {
                        editMode = EditMode.NONE
                        rebuildEditor()
                    }
                    .bounds(cardLeft + 4, actionRowY, cardWidth - 26, 14)
                    .build()
                )
            } else {
                addRenderableWidget(
                    Button.builder(Component.literal("Add by ID")) {
                        editMode = EditMode.AddById(group.id)
                    }
                    .bounds(cardLeft + 4, actionRowY, 70, 14)
                    .build()
                )
                addRenderableWidget(
                    Button.builder(Component.literal("Add by Tag")) {
                        editMode = EditMode.AddByTag(group.id)
                    }
                    .bounds(cardLeft + 76, actionRowY, 70, 14)
                    .build()
                )
                // Delete button — right-aligned on the same row
                addRenderableWidget(
                    Button.builder(Component.literal("Del")) {
                        deleteGroup(group)
                    }
                    .size(30, 14)
                    .bounds(panelX + backgroundWidth - 36, actionRowY, 30, 14)
                    .build()
                )
            }

            cardY += cardHeight + 2
        }

        // Bottom: +New only (changes are instant)
        val bottomY = panelY + backgroundHeight - 22
        addRenderableWidget(
            Button.builder(Component.literal("+ New")) { createNewGroup() }
                .bounds(panelX + 4, bottomY, 46, 16).build()
        )
    }

    private fun calculateGroupsPerPage(): Int {
        // Content area: below page bar (panelY + 22) to bottom buttons (panelY + backgroundHeight - 26)
        val availableHeight = backgroundHeight - 48
        var heightUsed = 0
        var count = 0
        for (group in StackGroups.groups) {
            val cardHeight = 32 + group.includes.size * 18 + 18 + 2  // name(12)+id(12)+gap(4)+padding(4) + slots + buttons + cardGap
            if (heightUsed + cardHeight > availableHeight) break
            heightUsed += cardHeight
            count++
        }
        return maxOf(1, count)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        tagOverlay?.let { if (it.mouseClicked(mouseX, mouseY, button)) return true }

        // In add mode: intercept clicks outside panel to capture ingredient,
        // don't forward to EMI (prevents recipe page navigation)
        if (editMode != EditMode.NONE && !inPanel(mouseX.toInt(), mouseY.toInt())) {
            handleAddModeClick(mouseX, mouseY)
            return true
        }

        if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) return true
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        EmiScreenManager.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button)

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dx: Double, dy: Double): Boolean =
        EmiScreenManager.mouseDragged(mouseX, mouseY, button, dx, dy) || super.mouseDragged(mouseX, mouseY, button, dx, dy)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (EmiScreenManager.mouseScrolled(mouseX, mouseY, scrollY)) return true
        if (inPanel(mouseX.toInt(), mouseY.toInt())) {
            val totalPages = maxOf(1, (StackGroups.groups.size + groupsPerPage - 1) / groupsPerPage)
            if (scrollY > 0) {
                currentPage = if (currentPage > 0) currentPage - 1 else totalPages - 1
            } else {
                currentPage = (currentPage + 1) % totalPages
            }
            rebuildEditor()
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) {
            if (editMode != EditMode.NONE) { editMode = EditMode.NONE; rebuildEditor(); return true }
            onClose(); return true
        }
        return EmiScreenManager.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean =
        EmiScreenManager.search.charTyped(chr, modifiers) || super.charTyped(chr, modifiers)

    private fun inPanel(mouseX: Int, mouseY: Int): Boolean =
        mouseX in (panelX - 8)..(panelX + backgroundWidth + 8) && mouseY in (panelY - 8)..(panelY + backgroundHeight + 8)

    override fun onClose() { StackGroups.saveAll(); super.onClose() }

    private fun handleAddModeClick(mouseX: Double, mouseY: Double) {
        val ingredient = EmiScreenManager.getHoveredStack(mouseX.toInt(), mouseY.toInt(), false).stack
        if (ingredient !is EmiStack || ingredient.isEmpty) return
        when (editMode) {
            is EditMode.AddById -> addById((editMode as EditMode.AddById).groupId, ingredient)
            is EditMode.AddByTag -> addByTag((editMode as EditMode.AddByTag).groupId, ingredient)
            else -> {}
        }
    }

    private fun addById(groupId: String, stack: EmiStack) {
        val group = StackGroups.groups.find { it.id == groupId } ?: return
        val registryType = when (stack.key) {
            is net.minecraft.world.item.Item -> "item"
            is net.minecraft.world.level.material.Fluid -> "fluid"
            else -> "item"
        }
        val notation = "$registryType:${stack.id}"
        if (group.includes.contains(notation)) return
        updateGroup(group, group.includes + notation)
    }

    private fun addByTag(groupId: String, stack: EmiStack) {
        val group = StackGroups.groups.find { it.id == groupId } ?: return
        val availableTags = getTagsForStack(stack)
        if (availableTags.isEmpty()) return
        tagOverlay = TagSelectionOverlay(width / 2 - 150, height / 2 - 100, 300, 200, availableTags) { selected ->
            tagOverlay = null
            if (selected.first.isNotEmpty() && !group.includes.contains(selected.first)) {
                updateGroup(group, group.includes + selected.first)
            }
        }
    }

    private fun getTagsForStack(stack: EmiStack): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        try {
            val registryKey = stack.key
            val adapter = EmiTags.ADAPTERS_BY_CLASS[registryKey::class.java] ?: return results
            for (tagKey in EmiTags.getTags(adapter.registry)) {
                if (tagKey.set.contains(registryKey)) {
                    val typePrefix = when (adapter.registry.key().location().toString()) {
                        "minecraft:item" -> "item"
                        "minecraft:fluid" -> "fluid"
                        else -> adapter.registry.key().location().path
                    }
                    val notation = "#$typePrefix:${tagKey.id()}"
                    if (results.none { it.first == notation }) results.add(notation to tagKey.id().toString())
                }
            }
        } catch (_: Exception) {}
        return results
    }

    private fun updateGroup(group: GroupConfig, includes: List<String>) {
        val idx = StackGroups.groups.indexOfFirst { it.id == group.id }
        if (idx >= 0) StackGroups.groups[idx] = group.copy(includes = includes)
        StackGroups.saveAll()
        StackGroups.reload()
        rebuildEditor()
    }

    /** Create native EMI ingredient: TagEmiIngredient for #tags, ListEmiIngredient for others */
    private fun createIngredient(selector: String): EmiIngredient {
        return if (selector.startsWith("#")) {
            EmiIngredientSerializer.getDeserialized(JsonPrimitive(selector))
        } else {
            val stacks = getPreviewStacks(selector)
            if (stacks.isEmpty()) EmiStack.EMPTY else EmiIngredient.of(stacks)
        }
    }

    private fun getPreviewStacks(notation: String): List<EmiStack> {
        val parsed = concerrox.minecraft.emiplusplus.group.GroupSelector.parse(notation) ?: return emptyList()
        return EmiStackList.stacks.filter { parsed.match(it) }.take(50)
    }

    private fun deleteGroup(group: GroupConfig) {
        StackGroups.groups.removeAll { it.id == group.id }
        StackGroups.saveAll()
        StackGroups.reload()
        val totalPages = maxOf(1, (StackGroups.groups.size + groupsPerPage - 1) / groupsPerPage)
        if (currentPage >= totalPages) currentPage = maxOf(0, totalPages - 1)
        rebuildEditor()
    }

    private fun createNewGroup() {
        StackGroups.groups.add(GroupConfig(
            name = "New Group",
            id = "emixx:custom_${System.currentTimeMillis() % 100000}",
            includes = emptyList()
        ))
        StackGroups.saveAll()
        StackGroups.reload()
        currentPage = maxOf(0, (StackGroups.groups.size - 1) / groupsPerPage)
        rebuildEditor()
    }
}
