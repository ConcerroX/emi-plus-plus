package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.config.EmiPlusPlusConfig;
import concerrox.emixx.content.ScreenManager;
import concerrox.emixx.content.creativemodetab.gui.CreativeModeTabGui;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class EmiScreenManagerSidebarPanelMixin {

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public List<EmiScreenManager.ScreenSpace> spaces;

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Shadow public abstract List<EmiScreenManager.ScreenSpace> getSpaces();

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/EmiRenderHelper;getPageText(III)Lnet/minecraft/network/chat/Component;", remap = true))
    private Component replaceIndexHeader(int page, int total, int maxWidth, Operation<Component> original) {
        if (getType() == SidebarType.INDEX && ScreenManager.INSTANCE.getCustomIndexTitle$emixx_neoforge_1_21_1() != null)
            return ScreenManager.INSTANCE.getCustomIndexTitle$emixx_neoforge_1_21_1();
        return original.call(page, total, maxWidth);
    }

    /**
     * Add extra space for the EMI++ header
     */
    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "drawBackground", name = "headerOffset")
    private int modifyHeaderOffset(int original) {
        if (this.spaces.stream().anyMatch(it -> it.getType() == SidebarType.INDEX) && EmiPlusPlusConfig.enableCreativeModeTabs.get()) {
            return original + CreativeModeTabGui.CREATIVE_MODE_TAB_HEIGHT;
        } else {
            return original;
        }
    }

    @Inject(at = @At("TAIL"), method = "setSpaces")
    private void addEmiPlusPlusWidgets(EmiScreenManager.ScreenSpace main, List<EmiScreenManager.ScreenSpace> subpanels, CallbackInfo ci) {
        if (getSpaces().stream().anyMatch(space -> space.getType() == SidebarType.INDEX)) {
            ScreenManager.INSTANCE.onIndexScreenSpaceCreated(space);
        }
    }

}
