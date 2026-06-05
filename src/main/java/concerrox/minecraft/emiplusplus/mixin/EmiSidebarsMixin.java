package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Redirect INDEX sidebar stack source for non-search panels.
 */
@Mixin(value = EmiSidebars.class, remap = false)
public class EmiSidebarsMixin {

    @Inject(method = "getStacks", at = @At("RETURN"), cancellable = true)
    private static void onGetStacks(SidebarType type, CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        if (type != SidebarType.INDEX) return;

        var assembler = StackGroups.INSTANCE.getAssembler();
        if (assembler != null) {
            cir.setReturnValue(StackGroups.INSTANCE.getIndexStacks());
        }
    }
}
