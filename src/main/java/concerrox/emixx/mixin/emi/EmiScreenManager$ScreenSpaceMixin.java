package concerrox.emixx.mixin.emi;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

import concerrox.emixx.content.StackManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;

@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class EmiScreenManager$ScreenSpaceMixin {

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    @Final
    public int tx;

    @Shadow
    @Final
    public int ty;

    @Shadow
    @Final
    public int tw;

    @Shadow
    @Final
    public int th;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createStackDisplayGrid(
            int tx, int ty, int tw, int th, boolean rtl, List<Bounds> exclusion, Supplier<SidebarType> typeSupplier,
            boolean search, CallbackInfo ci
    ) {
        if (getType() == SidebarType.INDEX) StackManager.getLayout().recreateLayout(tw, th);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(
            EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex,
            CallbackInfo ci
    ) {
        if (getType() == SidebarType.INDEX) {
            StackManager.getLayout().render(context.raw(), tx, ty);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;render(Ldev/emi/emi/api/stack/EmiIngredient;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void afterGetStack(
            EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex, CallbackInfo ci,
            @Local(name = "stack") EmiIngredient stack, @Local(name = "xo") int xo, @Local(name = "yo") int yo
    ) {
        if (getType() == SidebarType.INDEX) StackManager.getLayout().putStack(xo, yo, stack);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(
            EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex,
            CallbackInfo ci
    ) {
        if (getType() == SidebarType.INDEX) {
            final var layout = StackManager.getLayout();
//            layout.recreateLayout(tw, th);
            if (layout.getPageStartIndex() != startIndex) layout.setTilesDirty(true);
        }
    }

}
