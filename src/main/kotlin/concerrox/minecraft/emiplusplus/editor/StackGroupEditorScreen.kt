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
import net.minecraft.ChatFormatting
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
    private var hoveredGroupIndex: Int = -1
    private var hoveredSelectorGroup: GroupConfig? = null
    private var hoveredSelectorText: String? = null

    private var backgroundWidth = 176
    private var backgroundHeight = 200
    private var panelX = 0
    private var panelY = 0
    private var currentPage = 0
    private var pages: List<List<GroupConfig>> = emptyList()
    private var subPages: MutableMap<String, Int> = mutableMapOf()
    private var selectedGroupId: String? = null

    override fun init() {
        super.init()

        backgroundWidth = 176
        backgroundHeight = minOf(height - 40, 220)
        panelX = (width - backgroundWidth) / 2
        panelY = (height - backgroundHeight) / 2 + 1

        EmiScreenManager.addWidgets(this)
        bakePages()
        rebuildEditor()
    }

    /** RecipeScreen-style: pack groups into pages by height */
    private fun bakePages() {
        val maxHeight = backgroundHeight - 38 // top gap(19) + bottom(22) - overlap(3)
        val pages = mutableListOf<MutableList<GroupConfig>>()
        var current = mutableListOf<GroupConfig>()
        var heightUsed = 0

        for (group in StackGroups.groups) {
            val visibleSelectors = minOf(group.includes.size, 6)
            val cardH = 28 + 4 + visibleSelectors * 18 + 18 + 2

            if (current.isNotEmpty() && heightUsed + cardH > maxHeight) {
                pages.add(current)
                current = mutableListOf()
                heightUsed = 0
            }
            heightUsed += cardH
            current.add(group)
        }
        if (current.isNotEmpty()) pages.add(current)
        this.pages = pages
        if (currentPage >= pages.size) currentPage = maxOf(0, pages.size - 1)
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
        val totalPages = maxOf(1, pages.size)
        emiContext.drawCenteredTextWithShadow(
            EmiRenderHelper.getPageText(currentPage + 1, totalPages, backgroundWidth - 40),
            panelX + backgroundWidth / 2, panelY + 7, 0xFFFFFF
        )

        // Content: group cards with 9-patch backgrounds
        val visibleGroups = pages.getOrElse(currentPage) { emptyList() }
        var cardY = panelY + 19
        val cardLeft = panelX + 5
        val cardWidth = backgroundWidth - 10

        hoveredGroupIndex = -1
        var groupIdx = -1
        for (group in visibleGroups) {
            groupIdx++
            val selectorCount = group.includes.size
            val cardHeight = 28 + 4 + minOf(selectorCount, 6) * 18 + 18

            if (cardY + cardHeight > panelY + backgroundHeight - 26) break

            EmiRenderHelper.drawNinePatch(
                emiContext, TEXTURE,
                cardLeft, cardY, cardWidth, cardHeight,
                27, 0, 4, 1
            )

            // Green selection border (WidgetGroup error-border style)
            if (group.id == selectedGroupId) {
                emiContext.fill(cardLeft - 2, cardY - 3, cardWidth + 4, 2, 0xFF00AA00.toInt())
                emiContext.fill(cardLeft - 2, cardY + cardHeight + 1, cardWidth + 4, 2, 0xFF00AA00.toInt())
            }

            var lineY = cardY + 4
            // Group name always white with shadow
            graphics.drawString(font, group.name, cardLeft + 4, lineY, 0xFFFFFF)
            lineY += 10
            graphics.drawString(font, group.id, cardLeft + 4, lineY, 0x404040, false)
            lineY += 12 // font height + gap before slots

            // Sub-pagination for groups with many selectors (EmiIngredientRecipe pattern)
            val maxVisible = 6
            val totalSelectors = group.includes.size
            val subPage = subPages.getOrDefault(group.id, 0)
            val startIdx = subPage * maxVisible
            val visibleSelectors = group.includes.drop(startIdx).take(maxVisible)

            for (selector in visibleSelectors) {
                val ingredient = createIngredient(selector)
                val slotX = cardLeft + 4
                val slotY = lineY

                val textX = slotX + 22
                val textHovered = mouseX in textX..(textX + font.width(selector)) && mouseY in (slotY + 3)..(slotY + 14)
                if (textHovered) {
                    hoveredSelectorGroup = group
                    hoveredSelectorText = selector
                }

                val slot = SlotWidget(ingredient, slotX, slotY)
                slot.render(graphics, mouseX, mouseY, 0f)

                if (!ingredient.isEmpty && mouseX in slotX..slotX + 18 && mouseY in slotY..slotY + 18) {
                    val tooltip = slot.getTooltip(mouseX, mouseY)
                    if (tooltip.isNotEmpty()) {
                        EmiRenderHelper.drawTooltip(this, emiContext, tooltip, mouseX, mouseY)
                    }
                }

                if (textHovered) {
                    graphics.drawString(font, selector, textX, slotY + 5, 0xFFFFFF)
                    EmiRenderHelper.drawTooltip(
                        this, emiContext,
                        listOf(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(
                            Component.translatable("emixx.editor.pressDelete").visualOrderText
                        )),
                        mouseX, mouseY
                    )
                } else {
                    graphics.drawString(font, selector, textX, slotY + 5, 0x404040, false)
                }
                lineY += 18
            }

            // Sub-page indicator
            if (totalSelectors > maxVisible) {
                val totalSubPages = (totalSelectors + maxVisible - 1) / maxVisible
                graphics.drawString(
                    font,
                    "${subPage + 1}/$totalSubPages",
                    cardLeft + cardWidth - 30,
                    lineY - 10,
                    0x888888,
                    false
                )
                lineY += 4
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

        val totalPages = maxOf(1, pages.size)
        val visibleGroups = pages.getOrElse(currentPage) { emptyList() }

        // Page arrows — always visible, disabled when single page (RecipeScreen-style)
        val pageActive = totalPages > 1
        addRenderableWidget(
            SizedButtonWidget(panelX + 5, panelY + 5, 12, 12, 0, 0,
                { pageActive },
                { if (pageActive) { currentPage = if (currentPage > 0) currentPage - 1 else totalPages - 1; rebuildEditor() } }
            )
        )
        addRenderableWidget(
            SizedButtonWidget(panelX + backgroundWidth - 17, panelY + 5, 12, 12, 12, 0,
                { pageActive },
                { if (pageActive) { currentPage = (currentPage + 1) % totalPages; rebuildEditor() } }
            )
        )

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
            val cardHeight = 28 + 4 + minOf(selectorCount, 6) * 18 + 18

            // Sub-page arrows for overflow selectors
            val maxVisible = 6
            if (group.includes.size > maxVisible) {
                val totalSub = (group.includes.size + maxVisible - 1) / maxVisible
                val cur = subPages.getOrDefault(group.id, 0)
                val arrY = cardY + cardHeight - 18
                addRenderableWidget(
                    SizedButtonWidget(cardLeft + 4, arrY, 12, 12, 0, 0,
                        { true },
                        { subPages[group.id] = if (cur > 0) cur - 1 else totalSub - 1; rebuildEditor() }
                    )
                )
                addRenderableWidget(
                    SizedButtonWidget(cardLeft + 18, arrY, 12, 12, 12, 0,
                        { true },
                        { subPages[group.id] = (cur + 1) % totalSub; rebuildEditor() }
                    )
                )
            }

            // Selection click: clicking the card selects it
            // This is handled in render via the group name click detection
            cardY += cardHeight + 2
        }

        // Shared footer buttons
        val footerY = panelY + backgroundHeight - 22
        val hasSelection = selectedGroupId != null
        addRenderableWidget(
            Button.builder(Component.literal("Add ID")) {
                val gid = selectedGroupId ?: return@builder
                editMode = EditMode.AddById(gid)
            }
            .bounds(panelX + 4, footerY, 50, 16)
            .build().apply { active = hasSelection }
        )
        addRenderableWidget(
            Button.builder(Component.literal("Add Tag")) {
                val gid = selectedGroupId ?: return@builder
                editMode = EditMode.AddByTag(gid)
            }
            .bounds(panelX + 56, footerY, 50, 16)
            .build().apply { active = hasSelection }
        )
        addRenderableWidget(
            Button.builder(Component.literal("Del")) {
                val group = StackGroups.groups.find { it.id == selectedGroupId } ?: return@builder
                deleteGroup(group)
            }
            .bounds(panelX + 108, footerY, 30, 16)
            .build().apply { active = hasSelection }
        )
        addRenderableWidget(
            Button.builder(Component.literal("+").withStyle(net.minecraft.ChatFormatting.AQUA)) { createNewGroup() }
                .bounds(panelX + 140, footerY, 20, 16).build()
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        tagOverlay?.let { if (it.mouseClicked(mouseX, mouseY, button)) return true }

        // In add mode: intercept clicks outside panel to capture ingredient
        if (editMode != EditMode.NONE && !inPanel(mouseX.toInt(), mouseY.toInt())) {
            handleAddModeClick(mouseX, mouseY)
            return true
        }

        // Let widget clicks (sub-page arrows, footer buttons) process first
        if (super.mouseClicked(mouseX, mouseY, button)) return true

        // Click on a group card body → select/deselect it
        if (button == 0 && inPanel(mouseX.toInt(), mouseY.toInt())) {
            val clickedCard = findGroupAtPos(mouseX.toInt(), mouseY.toInt())
            if (clickedCard != null) {
                selectedGroupId = if (selectedGroupId == clickedCard.id) null else clickedCard.id
                rebuildEditor()
                return true
            }
        }

        if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) return true
        return false
    }

    private fun findGroupAtPos(mx: Int, my: Int): GroupConfig? {
        var cardY = panelY + 19
        val visibleGroups = pages.getOrElse(currentPage) { emptyList() }
        for (group in visibleGroups) {
            val cardHeight = 28 + 4 + minOf(group.includes.size, 6) * 18 + 18
            if (mx in (panelX + 5)..(panelX + backgroundWidth - 5) && my in cardY..(cardY + cardHeight)) {
                return group
            }
            cardY += cardHeight + 2
        }
        return null
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        EmiScreenManager.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button)

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dx: Double, dy: Double): Boolean =
        EmiScreenManager.mouseDragged(mouseX, mouseY, button, dx, dy) || super.mouseDragged(mouseX, mouseY, button, dx, dy)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (EmiScreenManager.mouseScrolled(mouseX, mouseY, scrollY)) return true
        if (inPanel(mouseX.toInt(), mouseY.toInt())) {
            val totalPages = maxOf(1, pages.size)
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
        // Delete/Backspace removes hovered selector
        if (keyCode == 261 || keyCode == 259 || scanCode == com.mojang.blaze3d.platform.InputConstants.KEY_DELETE) {
            val group = hoveredSelectorGroup
            val selector = hoveredSelectorText
            if (group != null && selector != null && group.includes.contains(selector)) {
                updateGroup(group, group.includes - selector)
                return true
            }
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
        // Don't add group icons themselves
        if (ingredient is concerrox.minecraft.emiplusplus.group.EmiGroupStack) return
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
        bakePages()
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
        // Delete the file from disk first
        val file = StackGroups.groupsDir().resolve(group.id.replace(":", "__").replace("/", "__") + ".json")
        try { java.nio.file.Files.deleteIfExists(file) } catch (_: Exception) {}
        // If it's a legacy file (no saveAll yet), file might not exist — fine
        StackGroups.groups.removeAll { it.id == group.id }
        StackGroups.bakeOnly()
        bakePages()
        if (currentPage >= pages.size) currentPage = maxOf(0, pages.size - 1)
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
        bakePages()
        currentPage = maxOf(0, pages.size - 1)
        rebuildEditor()
    }
}
