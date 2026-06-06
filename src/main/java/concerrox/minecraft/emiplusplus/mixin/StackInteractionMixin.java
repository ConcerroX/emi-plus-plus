package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.config.EmiPlusPlusKeyMappings;
import concerrox.minecraft.emiplusplus.editor.StackGroupEditorScreen;
import concerrox.minecraft.emiplusplus.group.EmiGroupStack;
import concerrox.minecraft.emiplusplus.group.GroupedEmiStackWrapper;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Intercepts clicks on grouped stacks and handles editor add mode capture.
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

        // Editor add mode: capture ingredient for the editor
        if (StackGroupEditorScreen.editorInAddMode && ingredient instanceof EmiStack realStack
            && !realStack.isEmpty() && !(ingredient instanceof EmiGroupStack)) {
            StackGroupEditorScreen.capturedIngredient = realStack;
            cir.setReturnValue(true);
            return;
        }

        if (ingredient instanceof EmiGroupStack groupStack) {
            if (function.apply(EmiBind.LEFT_CLICK)) {
                StackGroups.INSTANCE.toggle(groupStack);
                // Re-process search results: flatten + re-group with updated expand state
                if (EmiSearch.compiledQuery != null && !EmiSearch.compiledQuery.isEmpty()) {
                    var assembler = StackGroups.INSTANCE.getAssembler();
                    if (assembler != null && EmiSearch.stacks != null) {
                        List<EmiStack> flat = new ArrayList<>();
                        for (EmiIngredient ing : EmiSearch.stacks) {
                            if (ing instanceof EmiGroupStack gs) {
                                // Expand group icon to its members for re-grouping
                                for (var m : gs.getMembers()) flat.add(m.getRealStack());
                            } else if (ing instanceof EmiStack) {
                                flat.add((EmiStack) ing);
                            }
                        }
                        EmiSearch.stacks = assembler.search(flat);
                    }
                }
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
