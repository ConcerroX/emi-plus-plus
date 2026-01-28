package concerrox.emixx.mixin.emi;

import com.llamalad7.mixinextras.sugar.Local;
import concerrox.emixx.EmiPlusPlus;
import concerrox.emixx.config.EmiConfigScreenAttachment;
import concerrox.emixx.config.EmiPlusPlusConfigJumpButton;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.widget.config.ConfigSearch;
import dev.emi.emi.screen.widget.config.ListWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ConfigScreen.class)
public abstract class ConfigScreenMixin extends Screen {

    @Shadow
    private ConfigSearch search;

    @Shadow
    public abstract void jump(String jump);

    protected ConfigScreenMixin(Component title) {
        super(title);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/screen/ConfigScreen;addWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener onInit(GuiEventListener widget) {
        if (widget instanceof ListWidget list)
            EmiConfigScreenAttachment.attachConfigList(ConfigScreen.class.cast(this), list, search);
        return widget;
    }

    @Inject(method = "addJumpButtons", at = @At(value = "TAIL"))
    private void onAddJumpButtons(CallbackInfo ci, @Local(name = "y") int y, @Local(name = "v") int v) {
        addRenderableWidget(new EmiPlusPlusConfigJumpButton(
                2, y, w -> jump(EmiPlusPlus.MOD_ID),
                List.of(EmiConfigScreenAttachment.EMI_PLUS_PLUS_GROUP_NAME)
        ));
    }

}
