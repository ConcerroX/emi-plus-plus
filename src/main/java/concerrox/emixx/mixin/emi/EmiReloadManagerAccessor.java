package concerrox.emixx.mixin.emi;

import dev.emi.emi.runtime.EmiReloadManager;
import kotlin.NotImplementedError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EmiReloadManager.class)
public interface EmiReloadManagerAccessor {

    @Accessor("status")
    static void setStatus(int status) {
        throw new NotImplementedError();
    }

}
