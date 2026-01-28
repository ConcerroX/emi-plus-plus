package concerrox.emixx.mixin;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ModularUI.class)
public class ModularUIMixin {

    @Inject(method = "getGuiExtraAreas", at = @At("RETURN"), cancellable = true)
    private void onGetGuiExtraAreas(CallbackInfoReturnable<List<Rect2i>> cir) {
        // TODO: add check for this for only EMIScreenAttachment
        cir.setReturnValue(List.of());
    }

}
