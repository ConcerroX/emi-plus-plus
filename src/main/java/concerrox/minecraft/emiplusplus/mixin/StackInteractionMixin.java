package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.config.EmiPlusPlusKeyMappings;
import concerrox.minecraft.emiplusplus.group.EmiGroupStack;
import concerrox.minecraft.emiplusplus.group.GroupedEmiStackWrapper;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.screen.EmiScreenManager;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * Intercepts clicks and drag-and-drop on grouped stacks:
 * - EmiGroupStack: toggle expand/collapse on LEFT_CLICK
 * - GroupedEmiStackWrapper: Alt+Left collapses, normal clicks delegate to real stack,
 *   drag-and-drop unwraps pressedStack for favorites sidebar
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public class StackInteractionMixin {

    @Shadow
    private static EmiIngredient pressedStack;

    // -- Click handling --

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

        if (ingredient instanceof EmiGroupStack groupStack) {
            if (function.apply(EmiBind.LEFT_CLICK)) {
                StackGroups.INSTANCE.toggle(groupStack);
                cir.setReturnValue(true);
            }
            cir.setReturnValue(true);
            return;
        }

        if (ingredient instanceof GroupedEmiStackWrapper wrapper) {
            if (function.apply(EmiPlusPlusKeyMappings.collapseGroup)) {
                StackGroups.INSTANCE.collapse(wrapper.getGroupStack());
                cir.setReturnValue(true);
            } else if (stack instanceof EmiScreenManager.SidebarEmiStackInteraction sesi) {
                cir.setReturnValue(EmiScreenManager.stackInteraction(
                    new EmiScreenManager.SidebarEmiStackInteraction(
                        wrapper.getRealStack(), sesi.space
                    ),
                    function
                ));
            }
        }
    }

    // -- Drag-and-drop unwrapping --

    /**
     * After EMI sets pressedStack during mouseClicked, unwrap any
     * GroupedEmiStackWrapper so drag-to-favorites works correctly.
     */
    @Inject(
        method = "mouseClicked",
        at = @At(
            value = "FIELD",
            target = "Ldev/emi/emi/screen/EmiScreenManager;pressedStack:Ldev/emi/emi/api/stack/EmiIngredient;",
            opcode = Opcodes.PUTSTATIC,
            shift = At.Shift.AFTER
        )
    )
    private static void onPressedStackSet(CallbackInfoReturnable<Boolean> cir) {
        if (pressedStack instanceof GroupedEmiStackWrapper wrapper) {
            pressedStack = wrapper.getRealStack();
        }
    }
}
