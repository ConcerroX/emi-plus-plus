package concerrox.emixx.mixin.emi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import concerrox.emixx.content.ReloadManager;
import concerrox.emixx.content.stackgroup.StackGroups;

@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker", remap = false)
public class EmiReloadManager$ReloadWorkerMixin {

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiHidden;clear()V", shift = At.Shift.AFTER))
    public void onClear(CallbackInfo ci) {
        ReloadManager.clear();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Ldev/emi/emi/registry/EmiStackList;bake()V", shift = At.Shift.AFTER))
    public void afterStackListBaked(CallbackInfo ci) {
        ReloadManager.reloadAsync();
    }

}
