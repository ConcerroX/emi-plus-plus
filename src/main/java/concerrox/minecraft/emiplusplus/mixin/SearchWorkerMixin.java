package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.group.EmiGroupStack;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * After EMI search completes, inject group headers into the search results.
 * Strips existing EmiGroupStack entries so assembler.search() creates fresh ones.
 */
@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker", remap = false)
public class SearchWorkerMixin {

    @Inject(method = "run", at = @At("TAIL"))
    private void afterSearch(CallbackInfo ci) {
        try {
            var panel = EmiScreenManager.getSearchPanel();
            if (panel == null || panel.getType() != SidebarType.INDEX) return;

            List<? extends EmiIngredient> currentStacks = EmiSearch.stacks;
            if (currentStacks == null) return;

            // Flatten: expand EmiGroupStack to its members, keep other stacks as-is.
            // EMI search returns group icons but not their members — we need the members to re-group.
            List<EmiStack> stackList = new ArrayList<>();
            for (EmiIngredient ing : currentStacks) {
                if (ing instanceof EmiGroupStack groupStack) {
                    for (var member : groupStack.getMembers()) {
                        stackList.add(member.getRealStack());
                    }
                } else if (ing instanceof EmiStack) {
                    stackList.add((EmiStack) ing);
                }
            }

            var assembler = StackGroups.INSTANCE.getAssembler();
            if (assembler != null) {
                EmiSearch.stacks = assembler.search(stackList);
            }
        } catch (Exception ignored) {
        }
    }
}
