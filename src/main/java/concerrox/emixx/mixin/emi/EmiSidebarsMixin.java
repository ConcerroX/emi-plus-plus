package concerrox.emixx.mixin.emi;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import concerrox.emixx.content.StackManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiSidebars;

@Mixin(value = EmiSidebars.class, remap = false)
public class EmiSidebarsMixin {

//    @ModifyExpressionValue(method = "getStacks", at = @At(value = "FIELD", target = "Ldev/emi/emi/registry/EmiStackList;filteredStacks:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
//    private static List<? extends EmiIngredient> redirectStackSource(List<? extends EmiIngredient> original) {
//        return StackManager.getIndexStacks();
//    }

}
