package concerrox.emixx.mixin.emi;

import concerrox.emixx.config.EmiPlusPlusConfig;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SidebarButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiScreenManager.SidebarPanel.class)
public abstract class EmiScreenManager$SidebarPanelMixin {

    @Shadow
    public boolean header;

    @Shadow
    public abstract boolean isVisible();

    @Shadow
    @Final
    public SizedButtonWidget pageLeft;

    @Shadow
    @Final
    public SizedButtonWidget pageRight;

    @Shadow
    @Final
    public SidebarButtonWidget cycle;

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Inject(method = "updateWidgetPosition", at = @At("TAIL"))
    private void onUpdateWidgetPosition(CallbackInfo ci) {
        if (EmiPlusPlusConfig.hidePaginationButtons.get()) cycle.setX(space.tx);
    }

    @Inject(method = "updateWidgetVisibility", at = @At("TAIL"))
    private void onUpdateWidgetVisibility(CallbackInfo ci) {
        boolean isPaginationVisible = header && isVisible() && !EmiPlusPlusConfig.hidePaginationButtons.get();
        pageLeft.visible = isPaginationVisible;
        pageRight.visible = isPaginationVisible;
    }

    /**
     * @author ConcerroX
     * @reason Hide pagination buttons
     */
    @Overwrite
    private void drawHeader(EmiDrawContext context, int mouseX, int mouseY, float delta, int page, int totalPages) {
        if (!header) return;

        boolean hidePaginationButtons = EmiPlusPlusConfig.hidePaginationButtons.get();
        int entrySize = 18;

        Component text = EmiRenderHelper.getPageText(page + 1, totalPages, (space.tw - (hidePaginationButtons ? 1 : 3)) * entrySize);
        int x = space.tx + (space.tw * entrySize) / 2;
        int maxLeft = (space.tw - (hidePaginationButtons ? 1 : 2)) * entrySize / 2 - entrySize;
        int w = Minecraft.getInstance().font.width(text) / 2;
        if (w > maxLeft) {
            x += (w - maxLeft);
        }
        context.drawCenteredText(text, x, space.ty - 15);
        if (totalPages > 1 && space.tw > 2) {
            int scrollLeft = space.tx + (hidePaginationButtons ? 0 : 18);
            int scrollWidth = space.tw * entrySize - (hidePaginationButtons ? 0 : 36);
            int scrollY = space.ty - 4;
            context.fill(scrollLeft, scrollY, scrollWidth, 2, 0x55555555);
            EmiRenderHelper.drawScroll(context, scrollLeft, scrollY, scrollWidth, 2, page, totalPages, 0xFFFFFFFF);
        }
    }

}
