package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.config.EmiPlusPlusConfig;
import concerrox.emixx.content.Layout;
import concerrox.emixx.content.ScreenManager;
import concerrox.emixx.content.StackManager;
import concerrox.emixx.content.creativemodetab.gui.CreativeModeTabGui;
import concerrox.emixx.content.stackgroup.EmiGroupStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import kotlin.NotImplementedError;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {

    @Shadow
    private static List<? extends EmiIngredient> searchedStacks;

    @Shadow
    public static EmiScreenManager.SidebarPanel getSearchPanel() {
        throw new NotImplementedError();
    }

    /**
     * Add extra space for the EMI++ header
     */
    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "createScreenSpace", name = "headerOffset")
    private static int modifyHeaderOffset(int original, EmiScreenManager.SidebarPanel panel, Screen screen, List<Bounds> exclusion) {
        if (panel.getType() == SidebarType.INDEX && EmiPlusPlusConfig.enableCreativeModeTabs.get()) {
            return original + CreativeModeTabGui.CREATIVE_MODE_TAB_HEIGHT;
        } else {
            return original;
        }
    }

    /**
     * Redirect the stacks to EMI++'s stack manager:
     * {@snippet : searchedStacks = EmiSearch.stacks; } will be replaced by
     * {@snippet : searchedStacks = StackManager.displayedStacks; }
     */
    @Redirect(method = "recalculate",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/screen/EmiScreenManager;searchedStacks:Ljava/util/List;",
                    opcode = Opcodes.PUTSTATIC))
    private static void redirectStacksSourceToEmixx(List<? extends EmiIngredient> value) {
        searchedStacks = StackManager.INSTANCE.getDisplayedStacks$emixx_common();
    }

    /**
     * Redirect the stacks to EMI++'s stack manager:
     * {@snippet : searchedStacks != EmiSearch.stacks } will be replaced by
     * {@snippet : searchedStacks != StackManager.displayedStacks }
     */
    @ModifyExpressionValue(method = "recalculate",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/search/EmiSearch;stacks:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC))
    private static List<? extends EmiIngredient> redirectCachedStacksToEmixx(List<? extends EmiIngredient> original) {
        Layout.INSTANCE.setTextureDirty(true); // TODO: fix  this
        return StackManager.INSTANCE.getDisplayedStacks$emixx_common();
    }

    /**
     * Redirect the stacks to EMI++'s stack manager so it will search EMI++'s source stacks when searching the index
     */
    @Inject(method = "getSearchSource", at = @At(value = "RETURN"), cancellable = true)
    private static void redirectSearchSourceToEmixx(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        var panel = getSearchPanel();
        if (panel != null && panel.getType() == SidebarType.INDEX)
            cir.setReturnValue(StackManager.INSTANCE.getSourceStacks$emixx_common());
    }

    @Inject(at = @At("HEAD"), method = "addWidgets")
    private static void addEmiPlusPlusWidgets(Screen screen, CallbackInfo ci) {
        ScreenManager.INSTANCE.onScreenInitialized(screen);
    }

    @Inject(at = @At("RETURN"), method = "mouseScrolled", cancellable = true)
    private static void mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || ScreenManager.INSTANCE.onMouseScrolled(mouseX, mouseY, amount));
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;isEmpty()Z", ordinal = 0),
            method = "mouseReleased")
    private static boolean modifyMouseReleased(EmiIngredient instance, Operation<Boolean> original) {
        if (instance instanceof EmiGroupStack) StackManager.INSTANCE.onStackInteractionDeprecated(instance);
        return original.call(instance);
    }

}
