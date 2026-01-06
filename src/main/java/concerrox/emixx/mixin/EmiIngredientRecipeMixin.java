package concerrox.emixx.mixin;

import concerrox.emixx.content.StackManager;
import concerrox.emixx.content.stackgroup.StackGroupManager;
import concerrox.emixx.gui.components.ImageButton;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.EmiTagRecipe;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiIngredientRecipe.class, remap = false)
public class EmiIngredientRecipeMixin {

    @Inject(method = "addWidgets", at = @At("TAIL"))
    public void addCreateStackGroupButton(WidgetHolder widgets, CallbackInfo ci) {
        if (EmiIngredientRecipe.class.cast(this) instanceof EmiTagRecipe emiTagRecipe) {
            widgets.addButton(0, 0, 12, 12, 50, 0, ImageButton.Companion.getTEXTURE$emixx_neoforge_1_21_1(), () -> true, (b, n, k) -> {
                StackGroupManager.INSTANCE.create$emixx_neoforge_1_21_1((TagKey<Item>) emiTagRecipe.key);
                StackGroupManager.INSTANCE.reload$emixx_neoforge_1_21_1();
                StackManager.INSTANCE.reload$emixx_neoforge_1_21_1();
            });
            widgets.addTooltip(List.of(ClientTooltipComponent.create(Component.literal("Create Stack Group")
                    .getVisualOrderText())), 0, 0, 12, 12);
        }
    }

}
