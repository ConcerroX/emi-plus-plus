package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoMScreen.class, remap = false)
public class BoMScreenMixin extends Screen {

    protected BoMScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void render(GuiGraphics raw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.renderBackground(raw, mouseX, mouseY, delta);
    }

    @WrapOperation(
        at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V"), method = "render"
    )
    private void modifyMouseReleased(
        EmiDrawContext instance, int x, int y, int width, int height, int color,
        Operation<Void> original
    ) {
    }

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Ldev/emi/emi/screen/BoMScreen;renderMenuBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"
        ), method = "render"
    )
    private void modifyMouseReleased(BoMScreen instance, GuiGraphics guiGraphics, Operation<Void> original) {
    }

}
