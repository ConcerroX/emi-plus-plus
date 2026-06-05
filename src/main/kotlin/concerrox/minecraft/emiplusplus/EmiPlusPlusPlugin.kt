package concerrox.minecraft.emiplusplus

import concerrox.minecraft.emiplusplus.editor.StackGroupEditorScreen
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.widget.Bounds

@EmiEntrypoint
class EmiPlusPlusPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        registry.addScreenBoundsProvider(StackGroupEditorScreen::class.java) { screen ->
            // RecipeScreen-style: bounds = the editor panel, EMI goes around it
            val panelWidth = 220
            val mainHeight = minOf(screen.height - 40, 310)
            val bottomHeight = 32
            val totalHeight = mainHeight + 4 + bottomHeight
            val panelX = (screen.width - panelWidth) / 2
            val panelY = (screen.height - totalHeight) / 2 + 1
            Bounds(panelX, panelY - 26, panelWidth, totalHeight + 26)
        }
    }
}
