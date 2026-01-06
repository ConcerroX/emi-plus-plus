package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.content.villagertrade.AmountRangeItemEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.tooltip.RecipeCostTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeCostTooltipComponent.class, remap = false)
public class RecipeCostTooltipComponentMixin {

    @WrapOperation(method = "drawTooltip", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;drawStack(Ldev/emi/emi/api/stack/EmiIngredient;II)V"))
    private void removeAmountRange(EmiDrawContext instance, EmiIngredient stack, int x, int y, Operation<Void> original) {
        if (stack instanceof AmountRangeItemEmiStack) {
            instance.drawStack(stack, x, y, ~EmiIngredient.RENDER_AMOUNT);
        } else {
            original.call(instance, stack, x, y);
        }
    }

}
