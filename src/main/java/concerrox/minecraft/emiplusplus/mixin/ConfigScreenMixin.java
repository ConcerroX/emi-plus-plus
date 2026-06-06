package concerrox.minecraft.emiplusplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import concerrox.minecraft.emiplusplus.EmiPlusPlus;
import concerrox.minecraft.emiplusplus.config.EditorButtonEntry;
import concerrox.minecraft.emiplusplus.config.EmiPlusPlusConfig;
import concerrox.minecraft.emiplusplus.config.EmiPlusPlusKeyMappings;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.widget.config.BooleanWidget;
import dev.emi.emi.screen.widget.config.ConfigEntryWidget;
import dev.emi.emi.screen.widget.config.ConfigJumpButton;
import dev.emi.emi.screen.widget.config.ConfigSearch;
import dev.emi.emi.screen.widget.config.EmiBindWidget;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(value = ConfigScreen.class, remap = false)
public abstract class ConfigScreenMixin extends Screen {

    @Shadow
    private ConfigSearch search;

    @Shadow
    public abstract void jump(String jump);

    protected ConfigScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private static final Component EMI_PLUS_PLUS_GROUP = EmiPort.literal("E", Style.EMPTY.withColor(0xEB7BFC))
        .append(EmiPort.literal("M", Style.EMPTY.withColor(0x7BFCA2)))
        .append(EmiPort.literal("I", Style.EMPTY.withColor(0x7BEBFC)))
        .append(EmiPort.literal("++", Style.EMPTY.withColor(ChatFormatting.WHITE)));

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
        target = "Ldev/emi/emi/screen/ConfigScreen;addWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener onAddWidget(GuiEventListener widget) {
        if (widget instanceof ListWidget list) {
            attachEmiPlusPlusConfig(list);
        }
        return widget;
    }

    @Unique
    private void attachEmiPlusPlusConfig(ListWidget list) {
        ConfigScreen self = (ConfigScreen) (Object) this;

        var rootWidget = new GroupNameWidget(EmiPlusPlus.MOD_ID, EMI_PLUS_PLUS_GROUP);
        list.addEntry(rootWidget);

        var stackGroupsWidget = new SubGroupNameWidget(
            EmiPlusPlus.MOD_ID + ".stackGroups",
            Component.translatable("emixx.configuration.stackGroups")
        );
        stackGroupsWidget.parent = rootWidget;
        list.addEntry(stackGroupsWidget);

        // Enable Stack Groups
        var enableToggle = new BooleanWidget(
            Component.translatable("emixx.configuration.stackGroups.enable"),
            List.of(),
            search::getSearch,
            self.new Mutator<>() {
                @Override protected Boolean getValue() { return EmiPlusPlusConfig.INSTANCE.getStackGroupsEnabled(); }
                @Override protected void setValue(Boolean v) { EmiPlusPlusConfig.INSTANCE.setStackGroupsEnabled(v); StackGroups.INSTANCE.reload(); }
            }
        );
        addToGroups(list, enableToggle, rootWidget, stackGroupsWidget);

        // Show Group Border
        addToGroups(list, createBoolWidget(self, search,
            "emixx.configuration.stackGroups.showBorder",
            EmiPlusPlusConfig.INSTANCE::getShowGroupBorder,
            EmiPlusPlusConfig.INSTANCE::setShowGroupBorder),
            rootWidget, stackGroupsWidget);

        // Show Group Fill
        addToGroups(list, createBoolWidget(self, search,
            "emixx.configuration.stackGroups.showFill",
            EmiPlusPlusConfig.INSTANCE::getShowGroupFill,
            EmiPlusPlusConfig.INSTANCE::setShowGroupFill),
            rootWidget, stackGroupsWidget);

        // Collapse Group keybind
        var collapseBindWidget = new EmiBindWidget(self, List.of(),
            search::getSearch, EmiPlusPlusKeyMappings.collapseGroup);
        list.addEntry(collapseBindWidget);
        addToGroupsRaw(collapseBindWidget, rootWidget, stackGroupsWidget);

        // Show group member tooltip
        addToGroups(list, createBoolWidget(self, search,
            "emixx.configuration.stackGroups.memberTooltip",
            EmiPlusPlusConfig.INSTANCE::getShowGroupMemberTooltip,
            EmiPlusPlusConfig.INSTANCE::setShowGroupMemberTooltip),
            rootWidget, stackGroupsWidget);

        // Edit Groups button
        var editGroupsWidget = new EditorButtonEntry(search::getSearch);
        list.addEntry(editGroupsWidget);
        editGroupsWidget.endGroup = true;
        addToGroupsRaw(editGroupsWidget, rootWidget, stackGroupsWidget);
    }

    @Unique
    private static BooleanWidget createBoolWidget(
        ConfigScreen screen, ConfigSearch search, String key,
        Supplier<Boolean> getter, Consumer<Boolean> setter
    ) {
        return new BooleanWidget(Component.translatable(key), List.of(),
            search::getSearch, screen.new Mutator<>() {
                @Override protected Boolean getValue() { return getter.get(); }
                @Override protected void setValue(Boolean v) { setter.accept(v); }
            });
    }

    @Unique
    private static void addToGroups(
        ListWidget list, ConfigEntryWidget widget,
        GroupNameWidget root, SubGroupNameWidget sub
    ) {
        list.addEntry(widget);
        root.children.add(widget);
        widget.parentGroups.add(root);
        sub.children.add(widget);
        widget.parentGroups.add(sub);
    }

    @Unique
    private static void addToGroupsRaw(
        ConfigEntryWidget widget,
        GroupNameWidget root, SubGroupNameWidget sub
    ) {
        root.children.add(widget);
        widget.parentGroups.add(root);
        sub.children.add(widget);
        widget.parentGroups.add(sub);
    }

    @Inject(method = "addJumpButtons", at = @At(value = "TAIL"))
    private void onAddJumpButtons(CallbackInfo ci, @Local(name = "y") int y, @Local(name = "v") int v) {
        addRenderableWidget(new ConfigJumpButton(2, y, 0, v, w -> jump(EmiPlusPlus.MOD_ID),
            List.of(EMI_PLUS_PLUS_GROUP)));
    }
}
