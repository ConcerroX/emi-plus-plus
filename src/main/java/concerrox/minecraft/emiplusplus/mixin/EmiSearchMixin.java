package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.search.SearchGeneration;
import dev.emi.emi.search.EmiSearch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Records which search worker EMI actually accepted. EMI only stores a worker's
 * results when it is still the current worker; superseded workers are discarded.
 * The stamp is taken right after EMI writes EmiSearch.stacks, inside EMI's own
 * synchronized block, so it is set exactly for accepted workers.
 */
@Mixin(value = EmiSearch.class, remap = false)
public class EmiSearchMixin {

    @Inject(
            method = "apply",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/search/EmiSearch;stacks:Ljava/util/List;", opcode = org.objectweb.asm.Opcodes.PUTSTATIC, shift = At.Shift.AFTER)
    )
    private static void emixx$stampAccepted(@org.spongepowered.asm.mixin.injection.Coerce Object worker, List<?> stacks, CallbackInfo ci) {
        SearchGeneration.accept(worker);
    }
}
