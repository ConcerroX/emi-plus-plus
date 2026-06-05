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
            // Mark only the editor panel as content area with 8px margin.
            // EMI places panels around this area.
            val panelWidth = 360
            val panelMaxHeight = 380
            val panelX = (screen.width - panelWidth) / 2
            val panelY = maxOf(10, (screen.height - panelMaxHeight) / 2)
            val panelHeight = minOf(panelMaxHeight, screen.height - 20)
            Bounds(panelX - 8, panelY - 8, panelWidth + 16, panelHeight + 16)
        }
    }
}
