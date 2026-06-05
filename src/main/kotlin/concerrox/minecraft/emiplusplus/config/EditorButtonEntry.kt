package concerrox.minecraft.emiplusplus.config

import concerrox.minecraft.emiplusplus.editor.StackGroupEditorScreen
import dev.emi.emi.EmiPort
import dev.emi.emi.screen.widget.config.ConfigEntryWidget
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

/**
 * Config entry that shows an "Edit Groups" button opening the editor screen.
 * Must be in a non-mixin package to avoid Mixin's class-loading restrictions.
 */
class EditorButtonEntry(
    search: () -> String,
) : ConfigEntryWidget(
    Component.translatable("emixx.configuration.stackGroups.edit"),
    listOf(),
    search,
    20
) {
    private val editBtn: Button = EmiPort.newButton(0, 0, 150, 20,
        Component.translatable("emixx.configuration.stackGroups.edit"),
        { Minecraft.getInstance().setScreen(StackGroupEditorScreen()) }
    ).apply { active = true }

    init {
        setChildren(listOf(editBtn))
    }

    override fun update(y: Int, x: Int, width: Int, height: Int) {
        editBtn.x = x + width - editBtn.width
        editBtn.y = y
    }
}
