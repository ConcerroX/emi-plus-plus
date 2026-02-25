package concerrox.emixx.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Deprecated
@Mixin(Screen.class)
public interface ScreenAccessor {

    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidgetExternal(T widget);

    @Invoker("addWidget")
    <T extends GuiEventListener & NarratableEntry> T addWidgetExternal(T widget);

    @Invoker("removeWidget")
    void removeWidgetExternal(GuiEventListener widget);

}
