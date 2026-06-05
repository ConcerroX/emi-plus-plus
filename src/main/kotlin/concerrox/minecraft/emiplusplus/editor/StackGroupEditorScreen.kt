package concerrox.minecraft.emiplusplus.editor

import concerrox.minecraft.emiplusplus.group.GroupConfig
import concerrox.minecraft.emiplusplus.group.StackGroups
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
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
        private const val MAX_VISIBLE_SELECTORS = 6
    }

    var editMode: EditMode = EditMode.NONE
    internal var tagOverlay: TagSelectionOverlay? = null
    internal var hoveredSelectorGroup: GroupConfig? = null
    internal var hoveredSelectorText: String? = null

    internal var backgroundWidth = 176
    internal var backgroundHeight = 200
    internal var panelX = 0
    internal var panelY = 0
    internal var currentPage = 0
    internal var pages: List<List<GroupConfig>> = emptyList()
    internal var subPages: MutableMap<String, Int> = mutableMapOf()
    internal var selectedGroupId: String? = null

    // -- Init --

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

    // -- Render --

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
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

        // EMI overlay
        EmiScreenManager.drawBackground(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.render(emiContext, mouseX, mouseY, delta)
        EmiScreenManager.drawForeground(emiContext, mouseX, mouseY, delta)

        tagOverlay?.render(graphics, mouseX, mouseY)
        super.render(graphics, mouseX, mouseY, delta)
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

            // Selection border (WidgetGroup style)
            if (group.id == selectedGroupId) {
                emiContext.fill(cardLeft - 2, cardY - 3, cardWidth + 4, 2, 0xFF00AA00.toInt())
                emiContext.fill(cardLeft - 2, cardY + cardHeight + 1, cardWidth + 4, 2, 0xFF00AA00.toInt())
            }

            var lineY = cardY + 4
            // Group name (white + shadow), ID
            graphics.drawString(font, group.name, cardLeft + 4, lineY, 0xFFFFFF)
            lineY += 10
            graphics.drawString(font, group.id, cardLeft + 4, lineY, 0x404040, false)
            lineY += 12

            // Selector slots with sub-pagination
            val totalSelectors = group.includes.size
            val subPage = subPages.getOrDefault(group.id, 0)
            val startIdx = subPage * MAX_VISIBLE_SELECTORS
            val visibleSelectors = group.includes.drop(startIdx).take(MAX_VISIBLE_SELECTORS)

            for (selector in visibleSelectors) {
                renderSelectorRow(graphics, emiContext, selector, group, cardLeft, lineY, mouseX, mouseY)
                lineY += 18
            }

            // Sub-page counter
            if (totalSelectors > MAX_VISIBLE_SELECTORS) {
                val totalSub = (totalSelectors + MAX_VISIBLE_SELECTORS - 1) / MAX_VISIBLE_SELECTORS
                graphics.drawString(font, "${subPage + 1}/$totalSub", cardLeft + cardWidth - 30, lineY - 8, 0x888888, false)
            }

            // Add mode indicator
            if (editMode != EditMode.NONE) {
                val eid = (editMode as? EditMode.AddById)?.groupId ?: (editMode as? EditMode.AddByTag)?.groupId ?: ""
                if (eid == group.id) {
                    graphics.drawString(font, "Add mode active", cardLeft + 4, lineY, 0x00AA00, false)
                }
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
            { if (pageActive) { currentPage = if (currentPage > 0) currentPage - 1 else totalPages - 1; rebuildEditor() } }
        ))
        addRenderableWidget(SizedButtonWidget(panelX + backgroundWidth - 17, panelY + 5, 12, 12, 12, 0,
            { pageActive },
            { if (pageActive) { currentPage = (currentPage + 1) % totalPages; rebuildEditor() } }
        ))

        if (totalGroups == 0) {
            addRenderableWidget(Button.builder(Component.literal("+ New Group")) { createNewGroup() }
                .bounds(panelX + 4, panelY + backgroundHeight - 22, backgroundWidth - 8, 18).build())
            return
        }

        // Sub-page arrows per group
        var cardY = panelY + 19
        val cardLeft = panelX + 5
        for (group in visibleGroups) {
            val cardHeight = cardHeight(group.includes.size) - 2
            if (group.includes.size > MAX_VISIBLE_SELECTORS) {
                val totalSub = (group.includes.size + MAX_VISIBLE_SELECTORS - 1) / MAX_VISIBLE_SELECTORS
                val cur = subPages.getOrDefault(group.id, 0)
                addRenderableWidget(SizedButtonWidget(cardLeft + 4, cardY + cardHeight - 18, 12, 12, 0, 0,
                    { true },
                    { subPages[group.id] = if (cur > 0) cur - 1 else totalSub - 1; rebuildEditor() }
                ))
                addRenderableWidget(SizedButtonWidget(cardLeft + 18, cardY + cardHeight - 18, 12, 12, 12, 0,
                    { true },
                    { subPages[group.id] = (cur + 1) % totalSub; rebuildEditor() }
                ))
            }
            cardY += cardHeight + 2
        }

        // Shared footer
        val footerY = panelY + backgroundHeight - 22
        val sel = selectedGroupId != null
        addRenderableWidget(Button.builder(Component.literal("Add ID")) { selectedGroupId?.let { editMode = EditMode.AddById(it) } }
            .bounds(panelX + 4, footerY, 50, 16).build().apply { active = sel })
        addRenderableWidget(Button.builder(Component.literal("Add Tag")) { selectedGroupId?.let { editMode = EditMode.AddByTag(it) } }
            .bounds(panelX + 56, footerY, 50, 16).build().apply { active = sel })
        addRenderableWidget(Button.builder(Component.literal("Del")) { selectedGroupId?.let { gid -> StackGroups.groups.find { it.id == gid }?.let { deleteGroup(it) } } }
            .bounds(panelX + 108, footerY, 30, 16).build().apply { active = sel })
        addRenderableWidget(Button.builder(Component.literal("+").withStyle(ChatFormatting.AQUA)) { createNewGroup() }
            .bounds(panelX + 140, footerY, 20, 16).build())
    }

    // -- Input --

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        tagOverlay?.let { if (it.mouseClicked(mouseX, mouseY, button)) return true }
        if (editMode != EditMode.NONE && !inPanel(mouseX.toInt(), mouseY.toInt())) {
            handleAddModeClick(mouseX, mouseY); return true
        }
        if (super.mouseClicked(mouseX, mouseY, button)) return true
        if (button == 0 && inPanel(mouseX.toInt(), mouseY.toInt())) {
            val card = findGroupAtPos(mouseX.toInt(), mouseY.toInt())
            if (card != null) {
                selectedGroupId = if (selectedGroupId == card.id) null else card.id
                rebuildEditor(); return true
            }
        }
        if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) return true
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
            rebuildEditor(); return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) {
            if (editMode != EditMode.NONE) { editMode = EditMode.NONE; rebuildEditor(); return true }
            onClose(); return true
        }
        if (keyCode == 261 || keyCode == 259) {
            val grp = hoveredSelectorGroup; val sel = hoveredSelectorText
            if (grp != null && sel != null && grp.includes.contains(sel)) { updateGroup(grp, grp.includes - sel); return true }
        }
        return EmiScreenManager.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean =
        EmiScreenManager.search.charTyped(chr, modifiers) || super.charTyped(chr, modifiers)

    internal fun inPanel(mx: Int, my: Int): Boolean =
        mx in (panelX - 8)..(panelX + backgroundWidth + 8) && my in (panelY - 8)..(panelY + backgroundHeight + 8)

    override fun onClose() { StackGroups.saveAll(); super.onClose() }
}
