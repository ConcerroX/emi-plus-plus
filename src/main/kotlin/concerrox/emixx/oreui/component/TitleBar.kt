package concerrox.emixx.oreui.component

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.Minecraft
import concerrox.emixx.oreui.OreUi
import concerrox.emixx.id
import concerrox.emixx.text
import concerrox.emixx.util.blitOreUi
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class TitleBar(title: Component, onNavigateUp: () -> Unit) :
    AbstractContainerWidget(0, 0, 0, TITLE_BAR_HEIGHT, OreUi.createTitleComponent(title)), Layout {

    companion object {
        const val TITLE_BAR_HEIGHT = 25
    }

    private val backButton = IconButton(
        id("textures/gui/sprites/icon_button/icon_back_on_light.png"), text("gui", "navigate_up")
    ) { onNavigateUp() }

    override fun children() = listOf(backButton)
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

    override fun arrangeElements() {
        backButton.setPosition(x + 1, y + 1)
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        RenderSystem.enableBlend()
        RenderSystem.setShader(GameRenderer::getPositionTexShader)

        guiGraphics.drawCenteredString(Minecraft.font, message, width / 2, 8, 0x1e1e1f)
        guiGraphics.blitOreUi(x, y, width, height, 1, 1, 1, height)

        RenderSystem.disableBlend()
    }

    override fun visitWidgets(consumer: Consumer<AbstractWidget>) {
        children().stream().forEach(consumer)
    }

    override fun visitChildren(visitor: Consumer<LayoutElement>) {
        children().stream().forEach(visitor)
    }

}