package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.group.StackGroups;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Triggers group reload after EMI finishes its reload cycle.
 */
@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker", remap = false)
public class EmiReloadWorkerMixin {

    @Inject(method = "run", at = @At("TAIL"))
    private void afterEmiReload(CallbackInfo ci) {
        StackGroups.INSTANCE.reload();
    }
}
