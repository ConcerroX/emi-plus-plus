package concerrox.minecraft.emiplusplus.editor

import com.ibm.icu.text.Transliterator
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

/**
 * Overlay dialog for creating a new group. Card-style 9-patch background.
 * Coordinates are relative to the editor screen size and recomputed on each render.
 */
class NewGroupDialog(
    private val initialName: String = "",
    private val initialId: String = "emixx:custom/new_group",
    private val initialDesc: String = "",
    private val initialColor: String = "",
    private val onCreated: (name: String, id: String, description: String?, color: String?) -> Unit,
    private val onCancel: () -> Unit,
) {
    private val TEXTURE = EmiPort.id("emi", "textures/gui/background.png")
    private val font = Minecraft.getInstance().font
    private val transliterator = Transliterator.getInstance("Any-Latin;Latin-ASCII;")

    private val fields = mutableListOf<EditBox>()
    private var nameField: EditBox? = null
    private var idField: EditBox? = null
    private var descField: EditBox? = null
    private var colorField: EditBox? = null
    private var okBtn: Button? = null
    private var cancelBtn: Button? = null

    private var lastScreenW = 0
    private var lastScreenH = 0

    private fun layout(screenW: Int, screenH: Int) {
        if (screenW == lastScreenW && screenH == lastScreenH) return
        lastScreenW = screenW
        lastScreenH = screenH

        fields.clear()
        val dialogW = 280
        val dialogH = 150
        val x = (screenW - dialogW) / 2
        val y = (screenH - dialogH) / 2
        val labelX = x + 10
        val fieldX = x + 60
        val fieldW = dialogW - 64 // 60 left offset + 4 right margin
        val fieldH = 20

        nameField = makeField(fieldX, y + 28, fieldW, fieldH, initialName, "My Group")
        idField = makeField(fieldX, y + 50, fieldW, fieldH, initialId, "emixx:custom/my_group")
        descField = makeField(fieldX, y + 72, fieldW, fieldH, initialDesc, "Optional description")
        colorField = makeField(fieldX, y + 94, fieldW, fieldH, initialColor, "#AARRGGBB")

        // Override name responder: suggestion + ID auto-generation
        nameField?.setResponder { name ->
            nameField?.setSuggestion(if (name.isEmpty()) "My Group" else "")
            if (idField?.value?.startsWith("emixx:custom/") == true || idField?.value?.isBlank() == true) {
                val generated = transliterator.transform(name).lowercase()
                    .replace(" ", "_").replace(Regex("[^a-z0-9_.-]"), "")
                idField?.setValue("emixx:custom/$generated")
                idField?.setSuggestion("")
            }
        }

        val btnY = y + dialogH - 28
        okBtn = Button.builder(Component.literal("OK")) {
            val n = nameField?.value?.ifBlank { null } ?: return@builder
            val i = idField?.value?.ifBlank { null } ?: return@builder
            onCreated(n, i, descField?.value?.ifBlank { null }, colorField?.value?.ifBlank { null })
        }.bounds(x + 30, btnY, 100, 20).build()

        cancelBtn = Button.builder(Component.literal("Cancel")) { onCancel() }
            .bounds(x + 150, btnY, 100, 20).build()
    }

    private fun makeField(x: Int, y: Int, w: Int, h: Int, initial: String, suggestion: String): EditBox {
        val field = EditBox(font, x, y, w, h, Component.empty())
        field.setValue(initial)
        // If field has a value, don't show suggestion
        if (initial.isEmpty()) field.setSuggestion(suggestion)
        // On input: show suggestion only when empty
        field.setResponder { value ->
            field.setSuggestion(if (value.isEmpty()) suggestion else "")
        }
        fields.add(field)
        return field
    }

    fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val screenW = Minecraft.getInstance().screen?.width ?: return
        val screenH = Minecraft.getInstance().screen?.height ?: return
        layout(screenW, screenH)

        val dialogW = 280
        val dialogH = 150
        val x = (screenW - dialogW) / 2
        val y = (screenH - dialogH) / 2

        val emiContext = EmiDrawContext.wrap(graphics)
        EmiRenderHelper.drawNinePatch(emiContext, TEXTURE, x, y, dialogW, dialogH, 27, 0, 4, 1)

        graphics.drawString(font, "Name", x + 10, y + 32, 0xFFFFFF)
        graphics.drawString(font, "ID", x + 10, y + 54, 0xFFFFFF)
        graphics.drawString(font, "Desc", x + 10, y + 76, 0xFFFFFF)
        graphics.drawString(font, "Color", x + 10, y + 98, 0xFFFFFF)

        // Disable OK if name or id is blank
        val hasName = nameField?.value?.isNotBlank() == true
        val hasId = idField?.value?.isNotBlank() == true
        okBtn?.active = hasName && hasId

        for (f in fields) f.render(graphics, mouseX, mouseY, 0f)
        okBtn?.render(graphics, mouseX, mouseY, 0f)
        cancelBtn?.render(graphics, mouseX, mouseY, 0f)
    }

    fun mouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (button != 0) return true
        if (okBtn?.isMouseOver(mx, my) == true) {
            okBtn?.onPress()
            return true
        }
        if (cancelBtn?.isMouseOver(mx, my) == true) {
            cancelBtn?.onPress()
            return true
        }
        for (f in fields) {
            f.setFocused(f.isMouseOver(mx, my))
            f.mouseClicked(mx, my, button)
        }
        return true
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for (f in fields) {
            if (f.isFocused && f.keyPressed(keyCode, scanCode, modifiers)) return true
        }
        return false
    }

    fun charTyped(chr: Char, modifiers: Int): Boolean {
        for (f in fields) {
            if (f.isFocused && f.charTyped(chr, modifiers)) return true
        }
        return false
    }
}
