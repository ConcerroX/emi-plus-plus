package concerrox.emixx.mixin.emi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.content.stackgroup.EmiPlusPlusStackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker")
public class EmiSearch$SearchWorkerMixin {

    @WrapOperation(method = "run", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/search/EmiSearch;apply(Ldev/emi/emi/search/EmiSearch$SearchWorker;Ljava/util/List;)V"))
    public void run(EmiSearch.SearchWorker worker, List<? extends EmiIngredient> stacks, Operation<Void> original) {
        if (EmiScreenManager.getSearchPanel().getType() == SidebarType.INDEX) {
            original.call(worker, EmiPlusPlusStackGroups.indexStackGrouper.getPreGroupedStackList$emixx_neoforge_1_21_1());
//            StackManager.INSTANCE.buildStacks$emixx_neoforge_1_21_1((List<? extends EmiStack>) stacks);
        } else {
            original.call(worker, stacks);
        }
    }

}
