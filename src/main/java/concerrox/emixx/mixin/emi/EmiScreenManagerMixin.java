package concerrox.emixx.mixin.emi;

import com.llamalad7.mixinextras.sugar.Local;
import concerrox.emixx.content.EmiScreenAttachmentManager;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSettings;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiScreenManager.class, remap = false)
public class EmiScreenManagerMixin {

    @Inject(method = "recalculate", at = @At("TAIL"))
    private static void onRecalculate(CallbackInfo ci) {
        EmiScreenAttachmentManager.onMeasure();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private static void onRender(EmiDrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EmiScreenAttachmentManager.onRender(context.raw());
    }

    @Inject(method = "createScreenSpace", at = @At("TAIL"))
    private static void onCreateScreenSpace(EmiScreenManager.SidebarPanel panel, Screen screen, List<Bounds> exclusion,
                                            boolean rtl, Bounds bounds, SidebarSettings settings, CallbackInfo ci) {
        if (panel.side == EmiConfig.searchSidebar) EmiScreenAttachmentManager.onIndexScreenSpaceCreated(panel);
    }

    @ModifyVariable(method = "createScreenSpace", name = "headerOffset", at = @At(value = "STORE", ordinal = 0))
    private static int modifyCreateScreenSpaceHeaderOffset(int value,
                                                           @Local(argsOnly = true) EmiScreenManager.SidebarPanel panel) {
        if (panel.side == EmiConfig.searchSidebar) {
            return value + EmiScreenAttachmentManager.getHeader().getVisualHeight();
        } else {
            return value;
        }
    }

    @ModifyVariable(method = "createScreenSpace", name = "yMax", at = @At(value = "STORE", ordinal = 0))
    private static int modifyCreateScreenSpaceYMax(int value,
                                                   @Local(argsOnly = true) EmiScreenManager.SidebarPanel panel) {
        if (panel.side == EmiConfig.searchSidebar) {
            return value - EmiScreenAttachmentManager.getFooter().getVisualHeight();
        } else {
            return value;
        }
    }

}
