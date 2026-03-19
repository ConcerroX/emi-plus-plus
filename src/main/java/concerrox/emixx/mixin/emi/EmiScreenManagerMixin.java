package concerrox.emixx.mixin.emi;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.screens.Screen;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Function;

import concerrox.blueberry.compat.lowdraglib2.LowDragLib2;
import concerrox.emixx.content.EmiScreenAttachment;
import concerrox.emixx.content.StackManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSettings;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;

@Mixin(value = EmiScreenManager.class, remap = false)
public class EmiScreenManagerMixin {

    @Shadow
    private static List<? extends EmiIngredient> searchedStacks;

    @Shadow
    public static EmiIngredient pressedStack;

    @Inject(method = "recalculate", at = @At("TAIL"))
    private static void onRecalculate(CallbackInfo ci) {
        if (LowDragLib2.isInstalled()) EmiScreenAttachment.onMeasure();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private static void onRender(EmiDrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (LowDragLib2.isInstalled()) EmiScreenAttachment.onRender(context.raw());
    }

    @Inject(method = "createScreenSpace", at = @At("TAIL"))
    private static void onCreateScreenSpace(
            EmiScreenManager.SidebarPanel panel, Screen screen, List<Bounds> exclusion, boolean rtl, Bounds bounds,
            SidebarSettings settings, CallbackInfo ci
    ) {
        if (LowDragLib2.isInstalled()) {
            if (panel.side == EmiConfig.searchSidebar) EmiScreenAttachment.onIndexScreenSpaceCreated(panel);
        }
    }

    @ModifyVariable(method = "createScreenSpace", name = "headerOffset", at = @At(value = "STORE", ordinal = 0))
    private static int modifyCreateScreenSpaceHeaderOffset(
            int value,
            @Local(argsOnly = true) EmiScreenManager.SidebarPanel panel
    ) {
        if (LowDragLib2.isInstalled()) {
            if (panel.side == EmiConfig.searchSidebar) return value - EmiScreenAttachment.getHeader().getVisualHeight();
        }
        return value;
    }

    @ModifyVariable(method = "createScreenSpace", name = "yMax", at = @At(value = "STORE", ordinal = 0))
    private static int modifyCreateScreenSpaceYMax(
            int value,
            @Local(argsOnly = true) EmiScreenManager.SidebarPanel panel
    ) {
        if (LowDragLib2.isInstalled()) {
            if (panel.side == EmiConfig.searchSidebar) return value + EmiScreenAttachment.getFooter().getVisualHeight();
        }
        return value;
    }

    @ModifyExpressionValue(method = "recalculate", at = @At(value = "FIELD", target = "Ldev/emi/emi/search/EmiSearch;stacks:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<? extends EmiIngredient> redirectIndexStacks(List<? extends EmiIngredient> original) {
        return StackManager.getIndexStacks();
    }

    @Inject(method = "stackInteraction", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;isEmpty()Z", ordinal = 0), cancellable = true)
    private static void onMouseReleased(
            EmiStackInteraction stack, Function<EmiBind, Boolean> function,
            CallbackInfoReturnable<Boolean> cir
    ) {
        final var ret = StackManager.onClickStack(pressedStack, function);
        if (ret) cir.setReturnValue(true);
    }

}
