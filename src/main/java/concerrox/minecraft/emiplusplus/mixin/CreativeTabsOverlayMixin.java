package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.config.EmiPlusPlusConfig;
import concerrox.minecraft.emiplusplus.creativetabs.gui.CreativeTabsOverlay;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.SidebarSettings;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiScreenManager.class, remap = false)
public class CreativeTabsOverlayMixin {

    @Unique
    private static CreativeTabsOverlay emixx$creativeTabsOverlay;

    @ModifyVariable(method = "createScreenSpace", at = @At(value = "STORE", ordinal = 0), name = "headerOffset")
    private static int emixx$addCreativeTabHeaderOffset(
        int original,
        EmiScreenManager.SidebarPanel panel,
        Screen screen,
        List<Bounds> exclusion,
        boolean rtl,
        Bounds bounds,
        SidebarSettings settings
    ) {
        if (panel.getType() == SidebarType.INDEX && EmiPlusPlusConfig.INSTANCE.getCreativeModeTabsEnabled()) {
            return original + CreativeTabsOverlay.CREATIVE_TAB_HEIGHT;
        }
        return original;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private static void beforeRender(EmiDrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!EmiPlusPlusConfig.INSTANCE.getCreativeModeTabsEnabled()) return;

        var panel = EmiScreenManager.getPanelFor(SidebarType.INDEX);
        if (panel == null || panel.space == null || !panel.isVisible()) return;

        int headerOffset = panel.header ? 18 : 0;
        int x = panel.space.tx;
        int y = panel.space.ty - headerOffset - CreativeTabsOverlay.CREATIVE_TAB_HEIGHT;
        int width = panel.space.tw * 18;
        emixx$creativeTabsOverlay = new CreativeTabsOverlay(x, y, width, () -> {
            StackGroups.INSTANCE.refreshForCreativeTab();
            return kotlin.Unit.INSTANCE;
        });
        emixx$creativeTabsOverlay.render(context.raw(), mouseX, mouseY, delta);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private static void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (emixx$creativeTabsOverlay != null && emixx$creativeTabsOverlay.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private static void onMouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (emixx$creativeTabsOverlay != null && emixx$creativeTabsOverlay.mouseScrolled(mouseX, mouseY, amount)) {
            cir.setReturnValue(true);
        }
    }
}
