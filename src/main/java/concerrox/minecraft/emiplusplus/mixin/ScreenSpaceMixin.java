package concerrox.minecraft.emiplusplus.mixin;

import concerrox.minecraft.emiplusplus.group.EmiGroupStack;
import concerrox.minecraft.emiplusplus.group.GroupedEmiStackWrapper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Renders group borders around expanded group members.
 */
@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class ScreenSpaceMixin {

    @Shadow @Final public int tx;
    @Shadow @Final public int ty;
    @Shadow @Final public int tw;
    @Shadow @Final public int th;

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    /**
     * Shadow the internal widths array to avoid reflection every frame.
     */
    @Shadow
    @Final
    private int[] widths;

    private static final int ENTRY_SIZE = 18;

    /**
     * Render group borders around expanded group members.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(
        EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex,
        CallbackInfo ci
    ) {
        if (getType() != SidebarType.INDEX) return;

        List<? extends EmiIngredient> stacks = getStacks();

        // Fast path: check if any group stacks are in the visible range
        if (!hasGroupStacks(stacks, startIndex)) return;

        int tw = this.tw;
        int th = this.th;
        int tx = this.tx;
        int ty = this.ty;
        int[] widths = this.widths;

        // Build 2D grid of the visible portion only
        int i = startIndex;
        EmiIngredient[][] grid = new EmiIngredient[th][tw];
        for (int yo = 0; yo < th; yo++) {
            int rowWidth = widths[yo];
            for (int xo = 0; xo < rowWidth; xo++) {
                if (i < stacks.size()) {
                    grid[yo][xo] = stacks.get(i++);
                }
            }
        }

        // Draw borders
        for (int yo = 0; yo < th; yo++) {
            for (int xo = 0; xo < tw; xo++) {
                EmiIngredient cell = grid[yo][xo];
                if (!(cell instanceof GroupedEmiStackWrapper wrapper)) continue;

                int px = tx + xo * ENTRY_SIZE;
                int py = ty + yo * ENTRY_SIZE;

                EmiGroupStack group = wrapper.getGroupStack();
                int borderColor = wrapper.getBorderColor();

                // Top edge
                if (yo == 0 || !isSameGroup(grid[yo - 1][xo], group)) {
                    context.fill(px, py, ENTRY_SIZE, 1, borderColor);
                }
                // Bottom edge
                if (yo == th - 1 || !isSameGroup(grid[yo + 1][xo], group)) {
                    context.fill(px, py + ENTRY_SIZE - 1, ENTRY_SIZE, 1, borderColor);
                }
                // Left edge
                if (xo == 0 || !isSameGroup(grid[yo][xo - 1], group)) {
                    context.fill(px, py, 1, ENTRY_SIZE, borderColor);
                }
                // Right edge
                if (xo == tw - 1 || !isSameGroup(grid[yo][xo + 1], group)) {
                    context.fill(px + ENTRY_SIZE - 1, py, 1, ENTRY_SIZE, borderColor);
                }
            }
        }
    }

    /**
     * Quick scan to check if any GroupedEmiStackWrappers exist in the visible range.
     * If none, we can skip the entire border rendering pass.
     */
    @Unique
    private boolean hasGroupStacks(List<? extends EmiIngredient> stacks, int startIndex) {
        int end = Math.min(startIndex + tw * th, stacks.size());
        for (int i = startIndex; i < end; i++) {
            if (stacks.get(i) instanceof GroupedEmiStackWrapper) return true;
        }
        return false;
    }

    @Unique
    private static boolean isSameGroup(EmiIngredient cell, EmiGroupStack group) {
        return cell instanceof GroupedEmiStackWrapper wrapper
            && wrapper.getGroupStack() == group;
    }
}
