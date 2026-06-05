package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inject at the top of EmiScreenManager.render() to sync our grouped list
 * BEFORE EMI's internal searchedStacks sync runs (line 138-141).
 * This ensures the grouped list is used for both rendering and hit-testing.
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public class EmiScreenManagerMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private static void beforeRender(EmiDrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var assembler = StackGroups.INSTANCE.getAssembler();
        if (assembler == null) return;

        // Sync grouped list to EmiSearch.stacks before EMI syncs searchedStacks
        if (EmiSearch.compiledQuery == null || EmiSearch.compiledQuery.isEmpty()) {
            var grouped = StackGroups.INSTANCE.getIndexStacks();
            if (grouped != null) {
                EmiSearch.stacks = grouped;
            }
        }
    }
}
