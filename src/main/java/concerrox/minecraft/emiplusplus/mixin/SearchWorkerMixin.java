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
 * After EMI search completes, replace EmiSearch.stacks with grouped results.
 * Flattens existing group icons back to members, then re-runs group assembler.
 */
@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker", remap = false)
public class SearchWorkerMixin {

    @Inject(method = "run", at = @At("TAIL"))
    private void afterSearch(CallbackInfo ci) {
        try {
            var panel = EmiScreenManager.getSearchPanel();
            if (panel == null || panel.getType() != SidebarType.INDEX) return;

            List<? extends EmiIngredient> current = EmiSearch.stacks;
            if (current == null) return;

            // Flatten: expand group icons to members, keep other stacks
            List<EmiStack> flat = new ArrayList<>();
            for (EmiIngredient ing : current) {
                if (ing instanceof EmiGroupStack gs) {
                    for (var m : gs.getMembers()) flat.add(m.getRealStack());
                } else if (ing instanceof EmiStack) {
                    flat.add((EmiStack) ing);
                }
            }

            var assembler = StackGroups.INSTANCE.getAssembler();
            if (assembler != null) {
                EmiSearch.stacks = assembler.search(flat);
            }
        } catch (Exception ignored) {
        }
    }
}
