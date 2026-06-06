package concerrox.minecraft.emiplusplus.editor

import concerrox.minecraft.emiplusplus.group.GroupConfig
import concerrox.minecraft.emiplusplus.group.StackGroups
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.screen.widget.SizedButtonWidget
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.chat.Component

class StackGroupEditorScreen : Screen(Component.literal("EMI++ Group Editor")) {

    companion object {
        private val TEXTURE = EmiPort.id("emi", "textures/gui/background.png")
        private const val ROW_HEIGHT = 12
        private const val MAX_VISIBLE_SELECTORS = 12

        @JvmField
        var editorInAddMode: Boolean = false

        /** Captured by StackInteractionMixin when in add mode */
        @JvmField
        var capturedIngredient: EmiStack? = null
    }

    var editMode: EditMode = EditMode.NONE
        set(value) {
            field = value
            editorInAddMode = value != EditMode.NONE
        }
    internal var tagOverlay: TagSelectionOverlay? = null
    internal var hoveredSelectorGroup: GroupConfig? = null
    internal var hoveredSelectorText: String? = null

    internal var backgroundWidth = 220
    internal var backgroundHeight = 275
    internal var panelX = 0
    internal var panelY = 0
    internal var currentPage = 0
    internal var pages: List<List<GroupConfig>> = emptyList()
    internal var subPages: MutableMap<String, Int> = mutableMapOf()
    internal var selectedGroupId: String? = null

    // -- Init --

    override fun init() {
        super.init()
        backgroundWidth = 220
        backgroundHeight = minOf(height - 40, 310)
        val totalHeight = backgroundHeight + 4 + 32
        panelX = (width - backgroundWidth) / 2
        panelY = (height - totalHeight) / 2 + 1

        EmiScreenManager.addWidgets(this)
        bakePages()
        rebuildEditor()
    }

    // -- Render --

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Process any ingredient captured by StackInteractionMixin in add mode
        val captured = capturedIngredient
        if (captured != null && !captured.isEmpty) {
            capturedIngredient = null
            handleAddModeCaptured(captured)
        }

        renderTransparentBackground(graphics)
        val emiContext = EmiDrawContext.wrap(graphics)
        emiContext.resetColor()

        // Main panel
        EmiRenderHelper.drawNinePatch(emiContext, TEXTURE, panelX, panelY, backgroundWidth, backgroundHeight, 0, 0, 4, 1)

        // Page indicator
        EmiRenderHelper.drawNinePatch(emiContext, TEXTURE, panelX + 19, panelY + 5, backgroundWidth - 38, 12, 0, 16, 3, 6)
        val totalPages = maxOf(1, pages.size)
        emiContext.drawCenteredTextWithShadow(
            EmiRenderHelper.getPageText(currentPage + 1, totalPages, backgroundWidth - 40),
            panelX + backgroundWidth / 2, panelY + 7, 0xFFFFFF
        )

        // Group cards
        renderCards(graphics, emiContext, mouseX, mouseY, delta)

        // Bottom action panel (4px below list panel)
        val bottomY = panelY + backgroundHeight + 4
        EmiRenderHelper.drawNinePatch(emiContext, TEXTURE, panelX, bottomY, backgroundWidth, 28, 0, 0, 4, 1)

        // EMI overlay
        EmiScreenManager.drawBackground(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.render(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.drawForeground(emiContext, mouseX, mouseY, delta)

        super.render(graphics, mouseX, mouseY, delta)
        if (tagOverlay != null) {
            graphics.pose().pushPose()
            graphics.pose().translate(0f, 0f, 500f)
            tagOverlay?.render(graphics, mouseX, mouseY)
            graphics.pose().popPose()
        }
    }

    private fun renderCards(graphics: GuiGraphics, emiContext: EmiDrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val visibleGroups = pages.getOrElse(currentPage) { emptyList() }
        var cardY = panelY + 19
        val cardLeft = panelX + 5
        val cardWidth = backgroundWidth - 10

        hoveredSelectorGroup = null
        hoveredSelectorText = null

        for (group in visibleGroups) {
            val selectorCount = minOf(group.includes.size, MAX_VISIBLE_SELECTORS)
            val cardHeight = cardHeight(group.includes.size) - 2 // render height (no gap)

            if (cardY + cardHeight > panelY + backgroundHeight - 26) break

            // Card 9-patch background
            EmiRenderHelper.drawNinePatch(emiContext, TEXTURE, cardLeft, cardY, cardWidth, cardHeight, 27, 0, 4, 1)

            // Selection border (WidgetGroup style, inset within 9-patch corners)
            if (group.id == selectedGroupId) {
                emiContext.fill(cardLeft + 3, cardY + 1, cardWidth - 6, 2, 0xFF00AA00.toInt())
                emiContext.fill(cardLeft + 3, cardY + cardHeight - 3, cardWidth - 6, 2, 0xFF00AA00.toInt())
            }

            var lineY = cardY + 6
            graphics.drawString(font, group.name, cardLeft + 6, lineY, 0xFFFFFF)
            lineY += 10
            graphics.drawString(font, group.id, cardLeft + 6, lineY, 0x404040, false)
            lineY += 10

            // Selector slots with sub-pagination
            val totalSelectors = group.includes.size
            val subPage = subPages.getOrDefault(group.id, 0)
            val startIdx = subPage * MAX_VISIBLE_SELECTORS
            val visibleSelectors = group.includes.drop(startIdx).take(MAX_VISIBLE_SELECTORS)

            for (selector in visibleSelectors) {
                renderSelectorRow(graphics, emiContext, selector, group, cardLeft, lineY, mouseX, mouseY)
                lineY += 18
            }

            // Sub-page counter + arrows (top-right of card)
            if (totalSelectors > MAX_VISIBLE_SELECTORS) {
                val totalSub = (totalSelectors + MAX_VISIBLE_SELECTORS - 1) / MAX_VISIBLE_SELECTORS
                graphics.drawString(font, "${subPage + 1}/$totalSub", cardLeft + cardWidth - 54, cardY + 10, 0x888888, false)
            }

            cardY += cardHeight + 2
        }
    }

    private fun renderSelectorRow(
        graphics: GuiGraphics, emiContext: EmiDrawContext,
        selector: String, group: GroupConfig,
        cardLeft: Int, slotY: Int, mouseX: Int, mouseY: Int
    ) {
        val ingredient = createIngredient(selector)
        val slotX = cardLeft + 4
        val textX = slotX + 22
        val textHovered = mouseX in textX..(textX + font.width(selector)) && mouseY in (slotY + 3)..(slotY + 14)

        if (textHovered) {
            hoveredSelectorGroup = group
            hoveredSelectorText = selector
        }

        SlotWidget(ingredient, slotX, slotY).render(graphics, mouseX, mouseY, 0f)

        if (!ingredient.isEmpty && mouseX in slotX..slotX + 18 && mouseY in slotY..slotY + 18) {
            val tooltip = SlotWidget(ingredient, slotX, slotY).getTooltip(mouseX, mouseY)
            if (tooltip.isNotEmpty()) EmiRenderHelper.drawTooltip(this, emiContext, tooltip, mouseX, mouseY)
        }

        if (textHovered) {
            graphics.drawString(font, selector, textX, slotY + 5, 0xFFFFFF)
            EmiRenderHelper.drawTooltip(this, emiContext,
                listOf(ClientTooltipComponent.create(Component.translatable("emixx.editor.pressDelete").visualOrderText)),
                mouseX, mouseY
            )
        } else {
            graphics.drawString(font, selector, textX, slotY + 5, 0x404040, false)
        }
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {}

    // -- Widgets --

    internal fun rebuildEditor() {
        clearWidgets()
        val totalGroups = StackGroups.groups.size
        val visibleGroups = pages.getOrElse(currentPage) { emptyList() }
        val totalPages = maxOf(1, pages.size)
        val pageActive = totalPages > 1

        // Page arrows (always visible, disabled when single page)
        addRenderableWidget(SizedButtonWidget(panelX + 5, panelY + 5, 12, 12, 0, 0,
            { pageActive },
            { if (pageActive) { currentPage = if (currentPage > 0) currentPage - 1 else totalPages - 1
                        rebuildEditor()
} }
        ))
        addRenderableWidget(SizedButtonWidget(panelX + backgroundWidth - 17, panelY + 5, 12, 12, 12, 0,
            { pageActive },
            { if (pageActive) { currentPage = (currentPage + 1) % totalPages
                        rebuildEditor()
} }
        ))

        if (totalGroups == 0) { /* skip card rendering, still show footer below */ }

        // Sub-page arrows per group (top-right of card)
        var cardY = panelY + 19
        val cardLeft = panelX + 5
        for (group in visibleGroups) {
            val cardHeight = cardHeight(group.includes.size)
            if (group.includes.size > MAX_VISIBLE_SELECTORS) {
                val totalSub = (group.includes.size + MAX_VISIBLE_SELECTORS - 1) / MAX_VISIBLE_SELECTORS
                val cur = subPages.getOrDefault(group.id, 0)
                addRenderableWidget(SizedButtonWidget(cardLeft + backgroundWidth - 43, cardY + 7, 12, 12, 0, 0,
                    { true },
                    { subPages[group.id] = if (cur > 0) cur - 1 else totalSub - 1
                        rebuildEditor()
}
                ))
                addRenderableWidget(SizedButtonWidget(cardLeft + backgroundWidth - 29, cardY + 7, 12, 12, 12, 0,
                    { true },
                    { subPages[group.id] = (cur + 1) % totalSub
                        rebuildEditor()
}
                ))
            }
            cardY += cardHeight + 2
        }

        // Bottom action panel
        val actionY = panelY + backgroundHeight + 8
        val sel = selectedGroupId != null
        val inIdMode = editMode is EditMode.AddById && (editMode as EditMode.AddById).groupId == selectedGroupId
        val inTagMode = editMode is EditMode.AddByTag && (editMode as EditMode.AddByTag).groupId == selectedGroupId
        addRenderableWidget(Button.builder(Component.literal(if (inIdMode) "> ID" else "ID")) {
            if (selectedGroupId == null) return@builder
            editMode = if (inIdMode) EditMode.NONE else EditMode.AddById(selectedGroupId!!)
            rebuildEditor()
        }
            .bounds(panelX + 4, actionY, 49, 20).build().apply { active = sel })
        addRenderableWidget(Button.builder(Component.literal(if (inTagMode) "> Tag" else "Tag")) {
            if (selectedGroupId == null) return@builder
            editMode = if (inTagMode) EditMode.NONE else EditMode.AddByTag(selectedGroupId!!)
            rebuildEditor()
        }
            .bounds(panelX + 52, actionY, 49, 20).build().apply { active = sel })
        addRenderableWidget(Button.builder(Component.literal("Delete")) { selectedGroupId?.let { gid -> StackGroups.groups.find { it.id == gid }?.let { deleteGroup(it) } } }
            .bounds(panelX + 103, actionY, 46, 20).build().apply { active = sel })
        addRenderableWidget(Button.builder(Component.literal("+").withStyle(ChatFormatting.AQUA)) { createNewGroup() }
            .bounds(panelX + backgroundWidth - 24, actionY, 20, 20).build())
    }

    // -- Input --

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        tagOverlay?.let { if (it.mouseClicked(mouseX, mouseY, button)) return true }

        if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) return true

        if (super.mouseClicked(mouseX, mouseY, button)) {
            if (editMode != EditMode.NONE) editMode = EditMode.NONE
            return true
        }

        if (button == 0 && inPanel(mouseX.toInt(), mouseY.toInt())) {
            val card = findGroupAtPos(mouseX.toInt(), mouseY.toInt())
            if (card != null) {
                selectedGroupId = if (selectedGroupId == card.id) null else card.id
                if (selectedGroupId != null) {
                    StackGroups.expandById(selectedGroupId!!)
                } else {
                    editMode = EditMode.NONE
                }
                rebuildEditor()
                return true
            }
        }

        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        EmiScreenManager.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button)

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dx: Double, dy: Double): Boolean =
        EmiScreenManager.mouseDragged(mouseX, mouseY, button, dx, dy) || super.mouseDragged(mouseX, mouseY, button, dx, dy)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (EmiScreenManager.mouseScrolled(mouseX, mouseY, scrollY)) return true
        if (inPanel(mouseX.toInt(), mouseY.toInt())) {
            val tp = maxOf(1, pages.size)
            currentPage = if (scrollY > 0) (if (currentPage > 0) currentPage - 1 else tp - 1) else (currentPage + 1) % tp
            rebuildEditor()
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) {
            if (editMode != EditMode.NONE) {
                editMode = EditMode.NONE
                rebuildEditor()
                return true
            }
            onClose()
            return true
        }
        if (keyCode == 261 || keyCode == 259) {
            val grp = hoveredSelectorGroup
            val sel = hoveredSelectorText
            if (grp != null && sel != null && grp.includes.contains(sel)) {
                updateGroup(grp, grp.includes - sel)
                return true
            }
        }
        return EmiScreenManager.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean =
        EmiScreenManager.search.charTyped(chr, modifiers) || super.charTyped(chr, modifiers)

    private fun handleAddModeCaptured(stack: EmiStack) {
        when (editMode) {
            is EditMode.AddById -> addById((editMode as EditMode.AddById).groupId, stack)
            is EditMode.AddByTag -> addByTag((editMode as EditMode.AddByTag).groupId, stack)
            else -> {}
        }
    }

    internal fun inPanel(mx: Int, my: Int): Boolean =
        mx in (panelX - 8)..(panelX + backgroundWidth + 8) && my in (panelY - 8)..(panelY + backgroundHeight + 8)

    override fun onClose() {
        StackGroups.saveAll()
        super.onClose()
    }
}
