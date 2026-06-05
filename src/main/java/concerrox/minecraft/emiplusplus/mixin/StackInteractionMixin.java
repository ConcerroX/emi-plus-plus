package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.config.EmiPlusPlusKeyMappings;
import concerrox.minecraft.emiplusplus.group.EmiGroupStack;
import concerrox.minecraft.emiplusplus.group.GroupedEmiStackWrapper;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.screen.EmiScreenManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * Intercepts clicks on EmiGroupStack (toggle expand/collapse)
 * and GroupedEmiStackWrapper (collapse via keybind).
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public class StackInteractionMixin {

    @Inject(
        method = "stackInteraction(Ldev/emi/emi/api/stack/EmiStackInteraction;Ljava/util/function/Function;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onStackInteraction(
        EmiStackInteraction stack,
        Function<EmiBind, Boolean> function,
        CallbackInfoReturnable<Boolean> cir
    ) {
        EmiIngredient ingredient = stack.getStack();

        // Click on group icon: toggle expand/collapse
        if (ingredient instanceof EmiGroupStack groupStack) {
            StackGroups.INSTANCE.toggle(groupStack);
            cir.setReturnValue(true);
            return;
        }

        // Alt+Click on expanded group member: collapse the group
        if (ingredient instanceof GroupedEmiStackWrapper wrapper) {
            if (function.apply(EmiPlusPlusKeyMappings.collapseGroup)) {
                StackGroups.INSTANCE.collapse(wrapper.getGroupStack());
                cir.setReturnValue(true);
            }
        }
    }
}
