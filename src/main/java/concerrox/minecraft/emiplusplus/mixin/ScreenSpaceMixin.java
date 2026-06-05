package concerrox.minecraft.emiplusplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import concerrox.minecraft.emiplusplus.group.EmiGroupStack;
import concerrox.minecraft.emiplusplus.group.GroupedEmiStackWrapper;
import concerrox.minecraft.emiplusplus.group.StackGroups;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Hooks into ScreenSpace to:
 * 1. Inject grouped stacks into EmiSearch.stacks before each render
 * 2. Render group borders around expanded group members
 */
@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class ScreenSpaceMixin {

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

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    /**
     * Render group borders after EMI has drawn all items.
     * Thin white border lines are drawn at edges where a grouped member
     * is adjacent to a non-group-member or the grid boundary.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(
        EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex,
        CallbackInfo ci
    ) {
        // Only render borders for INDEX panels
        if (getType() != SidebarType.INDEX) return;

        List<? extends EmiIngredient> stacks = getStacks();
        int tw = this.tw;
        int th = this.th;
        int tx = this.tx;
        int ty = this.ty;
        final int ENTRY_SIZE = 18;

        // Build a 2D grid of stacks indexed by grid position
        int i = startIndex;
        EmiIngredient[][] grid = new EmiIngredient[th][tw];
        for (int yo = 0; yo < th; yo++) {
            int rowWidth = getRowWidth(yo);
            for (int xo = 0; xo < rowWidth; xo++) {
                if (i < stacks.size()) {
                    grid[yo][xo] = stacks.get(i);
                    i++;
                }
            }
        }

        // Draw borders around grouped members
        for (int yo = 0; yo < th; yo++) {
            for (int xo = 0; xo < tw; xo++) {
                EmiIngredient cell = grid[yo][xo];
                if (!(cell instanceof GroupedEmiStackWrapper wrapper)) continue;

                int px = tx + xo * ENTRY_SIZE;
                int py = ty + yo * ENTRY_SIZE;

                EmiGroupStack group = wrapper.getGroupStack();

                // Top edge
                if (yo == 0 || !isSameGroup(grid[yo - 1][xo], group)) {
                    context.fill(px, py, ENTRY_SIZE, 1, 0x66FFFFFF);
                }
                // Bottom edge
                if (yo == th - 1 || !isSameGroup(grid[yo + 1][xo], group)) {
                    context.fill(px, py + ENTRY_SIZE - 1, ENTRY_SIZE, 1, 0x66FFFFFF);
                }
                // Left edge
                if (xo == 0 || !isSameGroup(grid[yo][xo - 1], group)) {
                    context.fill(px, py, 1, ENTRY_SIZE, 0x66FFFFFF);
                }
                // Right edge
                if (xo == tw - 1 || !isSameGroup(grid[yo][xo + 1], group)) {
                    context.fill(px + ENTRY_SIZE - 1, py, 1, ENTRY_SIZE, 0x66FFFFFF);
                }
            }
        }
    }

    private boolean isSameGroup(EmiIngredient cell, EmiGroupStack group) {
        if (cell instanceof GroupedEmiStackWrapper wrapper) {
            return wrapper.getGroupStack() == group;
        }
        return false;
    }

    /** Helper to get row width from the ScreenSpace widths array. */
    private int getRowWidth(int yo) {
        try {
            // ScreenSpace has an int[] widths field
            var field = EmiScreenManager.ScreenSpace.class.getDeclaredField("widths");
            field.setAccessible(true);
            int[] widths = (int[]) field.get(this);
            return widths[yo];
        } catch (Exception e) {
            return this.tw;
        }
    }
}
