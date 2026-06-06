package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sync grouped list to EmiSearch.stacks before EMI's searchedStacks sync.
 * When no search is active, EmiSearch.stacks = the full grouped list.
 * When search IS active, the SearchWorkerMixin handles grouping.
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public class EmiScreenManagerMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private static void beforeRender(EmiDrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var assembler = StackGroups.INSTANCE.getAssembler();
        if (assembler == null) return;

        // When no search: sync full grouped list. Reference check avoids per-frame write.
        boolean queryEmpty = EmiSearch.compiledQuery == null || EmiSearch.compiledQuery.isEmpty();
        if (queryEmpty && EmiSearch.stacks != StackGroups.INSTANCE.getIndexStacks()) {
            EmiSearch.stacks = StackGroups.INSTANCE.getIndexStacks();
        }
    }
}
