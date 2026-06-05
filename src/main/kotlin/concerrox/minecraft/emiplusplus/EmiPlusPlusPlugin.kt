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
            val panelWidth = 176
            val panelHeight = minOf(screen.height - 40, 220)
            val panelX = (screen.width - panelWidth) / 2
            val panelY = (screen.height - panelHeight) / 2 + 1
            Bounds(panelX, panelY - 26, panelWidth, panelHeight + 26)
        }
    }
}
