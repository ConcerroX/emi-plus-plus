package concerrox.minecraft.emiplusplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import concerrox.minecraft.emiplusplus.EmiPlusPlus;
import concerrox.minecraft.emiplusplus.config.EmiPlusPlusConfig;
import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.widget.config.BooleanWidget;
import dev.emi.emi.screen.widget.config.ConfigJumpButton;
import dev.emi.emi.screen.widget.config.ConfigSearch;
import dev.emi.emi.screen.widget.config.GroupNameWidget;
import dev.emi.emi.screen.widget.config.ListWidget;
import dev.emi.emi.screen.widget.config.SubGroupNameWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ConfigScreen.class, remap = false)
public abstract class ConfigScreenMixin extends Screen {

    @Shadow
    private ConfigSearch search;

    @Shadow
    public abstract void jump(String jump);

    protected ConfigScreenMixin(Component title) {
        super(title);
    }

    private static final Component EMI_PLUS_PLUS_GROUP = EmiPort.literal("E", Style.EMPTY.withColor(0xEB7BFC))
        .append(EmiPort.literal("M", Style.EMPTY.withColor(0x7BFCA2)))
        .append(EmiPort.literal("I", Style.EMPTY.withColor(0x7BEBFC)))
        .append(EmiPort.literal("++", Style.EMPTY.withColor(ChatFormatting.WHITE)));

    /**
     * Append EMI++ config section after EMI's own entries are built.
     */
    @ModifyArg(method = "init", at = @At(value = "INVOKE",
        target = "Ldev/emi/emi/screen/ConfigScreen;addWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener onAddWidget(GuiEventListener widget) {
        if (widget instanceof ListWidget list) {
            attachEmiPlusPlusConfig(list);
        }
        return widget;
    }

    private void attachEmiPlusPlusConfig(ListWidget list) {
        ConfigScreen self = (ConfigScreen) (Object) this;

        // Top-level category: EMI++
        var rootWidget = new GroupNameWidget(EmiPlusPlus.MOD_ID, EMI_PLUS_PLUS_GROUP);
        list.addEntry(rootWidget);

        // Sub-category: Stack Groups
        var stackGroupsWidget = new SubGroupNameWidget(
            EmiPlusPlus.MOD_ID + ".stackGroups",
            Component.translatable("emixx.configuration.stackGroups")
        );
        stackGroupsWidget.parent = rootWidget;
        list.addEntry(stackGroupsWidget);

        // Toggle: Enable Stack Groups
        var enableToggle = new BooleanWidget(
            Component.translatable("emixx.configuration.stackGroups.enable"),
            List.of(),
            () -> search.getSearch(),
            self.new Mutator<>() {
                @Override
                protected Boolean getValue() {
                    return EmiPlusPlusConfig.INSTANCE.getStackGroupsEnabled();
                }
                @Override
                protected void setValue(Boolean value) {
                    EmiPlusPlusConfig.INSTANCE.setStackGroupsEnabled(value);
                }
            }
        );
        list.addEntry(enableToggle);
        enableToggle.endGroup = true;

        rootWidget.children.add(enableToggle);
        enableToggle.parentGroups.add(rootWidget);

        stackGroupsWidget.children.add(enableToggle);
        enableToggle.parentGroups.add(stackGroupsWidget);
    }

    /**
     * Add EMI++ jump button at the bottom of the left sidebar.
     */
    @Inject(method = "addJumpButtons", at = @At(value = "TAIL"))
    private void onAddJumpButtons(CallbackInfo ci, @Local(name = "y") int y, @Local(name = "v") int v) {
        addRenderableWidget(new ConfigJumpButton(2, y, 0, v, w -> jump(EmiPlusPlus.MOD_ID),
            List.of(EMI_PLUS_PLUS_GROUP)));
    }
}
