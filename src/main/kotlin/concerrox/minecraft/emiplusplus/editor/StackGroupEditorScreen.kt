package concerrox.minecraft.emiplusplus.editor

import concerrox.minecraft.emiplusplus.group.GroupConfig
import concerrox.minecraft.emiplusplus.group.StackGroups
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiTags
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.screen.widget.SizedButtonWidget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.client.Minecraft

class StackGroupEditorScreen : Screen(Component.literal("EMI++ Group Editor")) {

    companion object {
        private val TEXTURE = EmiPort.id("emi", "textures/gui/background.png")
        private const val ROW_HEIGHT = 12
    }

    var editMode: EditMode = EditMode.NONE
    private var tagOverlay: TagSelectionOverlay? = null
    private var emiReady = false

    private var minimumWidth = 176
    private var backgroundWidth = 176
    private var backgroundHeight = 200
    private var panelX = 0
    private var panelY = 0
    private var selectedGroupIndex = 0

    private lateinit var prevArrow: SizedButtonWidget
    private lateinit var nextArrow: SizedButtonWidget

    override fun init() {
        super.init()

        minimumWidth = 176
        backgroundWidth = minimumWidth
        backgroundHeight = minOf(height - 40, 220)
        panelX = (width - backgroundWidth) / 2
        panelY = (height - backgroundHeight) / 2 + 1

        if (!emiReady) {
            EmiScreenManager.addWidgets(this)
            emiReady = true
        }

        // Arrow buttons — same style as RecipeScreen
        val hasMultipleGroups = StackGroups.groups.size > 1
        prevArrow = SizedButtonWidget(
            panelX + 5, panelY + 5, 12, 12, 0, 0,
            { hasMultipleGroups },
            { selectedGroupIndex = if (selectedGroupIndex > 0) selectedGroupIndex - 1 else StackGroups.groups.size - 1; repositionWidgets() }
        )
        nextArrow = SizedButtonWidget(
            panelX + backgroundWidth - 17, panelY + 5, 12, 12, 12, 0,
            { hasMultipleGroups },
            { selectedGroupIndex = (selectedGroupIndex + 1) % maxOf(1, StackGroups.groups.size); repositionWidgets() }
        )

        addRenderableWidget(prevArrow)
        addRenderableWidget(nextArrow)

        repositionWidgets()
    }

    private fun repositionWidgets() {
        // Re-position arrows
        prevArrow.x = panelX + 5
        prevArrow.y = panelY + 5
        nextArrow.x = panelX + backgroundWidth - 17
        nextArrow.y = panelY + 5
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Use Minecraft's standard translucent background — not a custom dark overlay
        renderTransparentBackground(graphics)

        val emiContext = EmiDrawContext.wrap(graphics)
        emiContext.resetColor()

        // Match RecipeScreen: render transparent layer then reset color before 9-patch

        val totalGroups = StackGroups.groups.size
        val group = StackGroups.groups.getOrNull(selectedGroupIndex)

        // Main panel — 9-patch background like RecipeScreen
        EmiRenderHelper.drawNinePatch(
            emiContext, TEXTURE, panelX, panelY, backgroundWidth, backgroundHeight,
            0, 0, 4, 1
        )

        // Category bar background (like RecipeScreen's category strip)
        EmiRenderHelper.drawNinePatch(
            emiContext, TEXTURE,
            panelX + 19, panelY + 5, backgroundWidth - 38, 12,
            0, 16, 3, 6
        )

        // Page indicator bar background
        EmiRenderHelper.drawNinePatch(
            emiContext, TEXTURE,
            panelX + 19, panelY + 19, backgroundWidth - 38, 12,
            0, 16, 3, 6
        )

        // Group name as category title
        val titleText = if (group != null) group.name else "No Groups"
        emiContext.drawCenteredTextWithShadow(
            Component.literal(titleText),
            panelX + backgroundWidth / 2, panelY + 7, 0xFFFFFF
        )

        // Page indicator
        emiContext.drawCenteredTextWithShadow(
            EmiRenderHelper.getPageText(
                if (totalGroups > 0) selectedGroupIndex + 1 else 0,
                totalGroups, backgroundWidth - 40
            ),
            panelX + backgroundWidth / 2, panelY + 21, 0xFFFFFF
        )

        // Content area
        val contentLeft = panelX + 10
        var contentY = panelY + 38

        if (group != null) {
            // Group ID
            graphics.drawString(font, group.id, contentLeft, contentY, 0x888888)
            contentY += ROW_HEIGHT + 4

            // Separator
            graphics.fill(contentLeft, contentY, contentLeft + backgroundWidth - 24, contentY + 1, 0x44888888.toInt())
            contentY += 6

            // Selectors with remove buttons
            for (selector in group.includes) {
                if (contentY > panelY + backgroundHeight - 40) break
                graphics.drawString(font, selector, contentLeft, contentY, 0x404040)
                contentY += ROW_HEIGHT + 2
            }

            // Add mode status
            if (editMode != EditMode.NONE) {
                val editingGroupId = (editMode as? EditMode.AddById)?.groupId
                    ?: (editMode as? EditMode.AddByTag)?.groupId ?: ""
                if (editingGroupId == group.id) {
                    contentY += 4
                    graphics.fill(contentLeft, contentY, contentLeft + backgroundWidth - 20, contentY + 12, 0x44007700.toInt())
                    graphics.drawString(font, "Click items in EMI to add. ESC to cancel.",
                        contentLeft + 2, contentY + 2, 0x00FF00)
                }
            }
        } else {
            graphics.drawCenteredString(font, "No groups yet. Create one to get started.",
                panelX + backgroundWidth / 2, contentY + 10, 0x888888)
        }

        // EMI overlay
        EmiScreenManager.drawBackground(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.render(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.drawForeground(emiContext, mouseX, mouseY, delta)

        tagOverlay?.render(graphics, mouseX, mouseY)
        super.render(graphics, mouseX, mouseY, delta)
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Use standard Minecraft dark overlay, same as RecipeScreen
        renderTransparentBackground(graphics)
    }

    private fun rebuildEditor() {
        clearWidgets()

        // Re-add arrows
        addRenderableWidget(prevArrow)
        addRenderableWidget(nextArrow)

        val totalGroups = StackGroups.groups.size
        val group = StackGroups.groups.getOrNull(selectedGroupIndex)

        if (group == null) {
            addRenderableWidget(
                Button.builder(Component.literal("+ New Group")) { createNewGroup() }
                    .bounds(panelX + 4, panelY + backgroundHeight - 22, backgroundWidth - 8, 18)
                    .build()
            )
        } else {
            val contentLeft = panelX + 8
            val contentWidth = backgroundWidth - 16
            var buttonY = panelY + 38 + ROW_HEIGHT + 4 + 1 + 6 // after id + separator

            // Remove buttons for each selector
            for (selector in group.includes) {
                if (buttonY > panelY + backgroundHeight - 60) break
                addRenderableWidget(
                    Button.builder(Component.literal("✕")) {
                        updateGroup(group, group.includes - selector)
                    }
                    .size(14, 12)
                    .bounds(panelX + backgroundWidth - 26, buttonY - 1, 14, 12)
                    .build()
                )
                buttonY += ROW_HEIGHT + 2
            }
            buttonY += 4

            // Add mode buttons
            if (editMode is EditMode.AddById && (editMode as EditMode.AddById).groupId == group.id ||
                editMode is EditMode.AddByTag && (editMode as EditMode.AddByTag).groupId == group.id
            ) {
                addRenderableWidget(
                    Button.builder(Component.literal("Cancel")) {
                        editMode = EditMode.NONE
                        rebuildEditor()
                    }
                    .bounds(contentLeft, buttonY, contentWidth, 16)
                    .build()
                )
                buttonY += 18
            } else {
                addRenderableWidget(
                    Button.builder(Component.literal("Add by ID")) {
                        editMode = EditMode.AddById(group.id)
                    }
                    .bounds(contentLeft, buttonY, contentWidth / 2 - 2, 16)
                    .build()
                )
                addRenderableWidget(
                    Button.builder(Component.literal("Add by Tag")) {
                        editMode = EditMode.AddByTag(group.id)
                    }
                    .bounds(contentLeft + contentWidth / 2 + 2, buttonY, contentWidth / 2 - 2, 16)
                    .build()
                )
                buttonY += 18
            }

            // Bottom buttons
            val bottomY = panelY + backgroundHeight - 22
            addRenderableWidget(
                Button.builder(Component.literal("+ New")) { createNewGroup() }
                    .bounds(panelX + 4, bottomY, 46, 16).build()
            )
            addRenderableWidget(
                Button.builder(Component.literal("Delete")) {
                    StackGroups.groups.removeAll { it.id == group.id }
                    StackGroups.saveAll()
                    StackGroups.reload()
                    if (selectedGroupIndex >= StackGroups.groups.size) {
                        selectedGroupIndex = maxOf(0, StackGroups.groups.size - 1)
                    }
                    rebuildEditor()
                }
                .bounds(panelX + 54, bottomY, 46, 16).build()
            )
            addRenderableWidget(
                Button.builder(Component.literal("Done")) { onClose() }
                    .bounds(panelX + backgroundWidth - 54, bottomY, 48, 16).build()
            )
        }

        repositionWidgets()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        tagOverlay?.let { if (it.mouseClicked(mouseX, mouseY, button)) return true }
        if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) {
            if (editMode != EditMode.NONE && !inPanel(mouseX.toInt(), mouseY.toInt())) {
                handleAddModeClick(mouseX, mouseY)
            }
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        EmiScreenManager.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button)

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dx: Double, dy: Double): Boolean =
        EmiScreenManager.mouseDragged(mouseX, mouseY, button, dx, dy) || super.mouseDragged(mouseX, mouseY, button, dx, dy)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (EmiScreenManager.mouseScrolled(mouseX, mouseY, scrollY)) return true
        // Scroll anywhere in panel to navigate between groups
        if (inPanel(mouseX.toInt(), mouseY.toInt()) && StackGroups.groups.size > 1) {
            if (scrollY > 0) {
                selectedGroupIndex = if (selectedGroupIndex > 0) selectedGroupIndex - 1 else StackGroups.groups.size - 1
            } else {
                selectedGroupIndex = (selectedGroupIndex + 1) % StackGroups.groups.size
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

    private fun createNewGroup() {
        StackGroups.groups.add(GroupConfig(
            name = "New Group",
            id = "emixx:custom_${System.currentTimeMillis() % 100000}",
            includes = emptyList()
        ))
        StackGroups.saveAll()
        StackGroups.reload()
        selectedGroupIndex = StackGroups.groups.size - 1
        rebuildEditor()
    }
}
