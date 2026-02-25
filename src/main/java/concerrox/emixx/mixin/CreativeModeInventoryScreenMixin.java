package concerrox.emixx.mixin;

import concerrox.emixx.content.creativemodetab.CreativeModeTabManager;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated
@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {

    @Inject(method = "selectTab", at = @At("HEAD"))
    private void onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeTabManager.INSTANCE.onCreativeModeInventoryScreenTabSelected$emixx_neoforge_1_21_1(tab);
    }

}
