package concerrox.emixx.mixin.emi;

import concerrox.emixx.content.ReloadManager;
import concerrox.emixx.content.stackgroup.EmiPlusPlusStackGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker")
public class EmiReloadManager$ReloadWorkerMixin {

    @Inject(method = "run",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiHidden;clear()V", shift = At.Shift.AFTER))
    public void onClear(CallbackInfo ci) {
        EmiPlusPlusStackGroups.clear();
    }

    @Inject(method = "run",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/registry/EmiStackList;bake()V", shift = At.Shift.AFTER))
    public void onStackListBaked(CallbackInfo ci) {
        ReloadManager.reloadAsync();
    }

}
