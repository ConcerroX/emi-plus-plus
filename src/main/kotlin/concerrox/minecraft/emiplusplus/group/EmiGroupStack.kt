package concerrox.minecraft.emiplusplus.group

import concerrox.minecraft.emiplusplus.Identifier
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.EmiUtil
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.tooltip.TagTooltipComponent
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * Group header icon that appears in the EMI sidebar grid.
 *
 * When collapsed: renders 3 member items stacked with cascading offsets + "+N" badge.
 * When expanded: renders a semi-transparent cell with a "-" badge.
 * Clicking toggles expand/collapse via [StackGroups].
 */
class EmiGroupStack(
    val groupId: String,
    val groupName: String,
) : EmiStack() {

    val members: MutableList<GroupedEmiStackWrapper> = mutableListOf()
    var isExpanded: Boolean = false

    /**
     * Add a member stack (wrapped). Only called during group assembly.
     */
    fun addMember(stack: EmiStack) {
        members += GroupedEmiStackWrapper(stack, this)
    }

    // -- EmiStack abstract methods --

    override fun getKey(): Any = groupId
    override fun getId(): Identifier = Identifier.parse(groupId)
    override fun isEmpty(): Boolean = false
    override fun getName(): Component = Component.literal(groupName)
    override fun getTooltipText(): MutableList<Component> = mutableListOf()
    override fun getComponentChanges(): DataComponentPatch = DataComponentPatch.EMPTY
    override fun getItemStack(): ItemStack = ItemStack.EMPTY
    override fun getAmount(): Long = 1L
    override fun setAmount(amount: Long): EmiStack = this
    override fun getChance(): Float = 1.0f
    override fun setChance(chance: Float): EmiStack = this

    override fun copy(): EmiStack {
        val copy = EmiGroupStack(groupId, groupName)
        copy.isExpanded = isExpanded
        copy.members.addAll(members)
        return copy
    }

    // -- Rendering --

    override fun render(raw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) {
        val context = EmiDrawContext.wrap(raw)
        context.push()

        if (isExpanded) {
            // Expanded: subtle background + thin border
            context.fill(x - 1, y - 1, 1, ENTRY_SIZE, 0xFFFFFFFF.toInt())        // left
            context.fill(x - 1, y - 1, ENTRY_SIZE, 1, 0xFFFFFFFF.toInt())        // top
            context.fill(x + ENTRY_SIZE - 2, y - 1, 1, ENTRY_SIZE, 0xFFFFFFFF.toInt()) // right
            context.fill(x - 1, y + ENTRY_SIZE - 2, ENTRY_SIZE, 1, 0xFFFFFFFF.toInt()) // bottom
            context.fill(x, y, ENTRY_SIZE - 2, ENTRY_SIZE - 2, 0x30FFFFFF)       // fill
        }

        context.push()
        context.matrices().translate(x.toFloat() + 1.6F, y.toFloat() + 1.6F, 0F)
        context.matrices().scale(0.8F, 0.8F, 0.8F)

        // Render up to 3 member items with stacked offsets
        when {
            members.size == 1 -> {
                members[0].realStack.render(raw, 0, 0, delta, flags)
            }
            members.size == 2 -> {
                context.matrices().translate(0.5F, 0F, 0F)
                members[1].realStack.render(raw, 1, -1, delta, flags)
                context.matrices().translate(0F, 0F, 10F)
                members[0].realStack.render(raw, -2, 1, delta, flags)
            }
            members.size >= 3 -> {
                members[2].realStack.render(raw, 3, -2, delta, flags)
                context.matrices().translate(0F, 0F, 10F)
                members[1].realStack.render(raw, 0, 0, delta, flags)
                context.matrices().translate(0F, 0F, 10F)
                members[0].realStack.render(raw, -3, 2, delta, flags)
            }
        }
        context.pop()

        // +/- badge in bottom-right
        EmiRenderHelper.renderAmount(context, x, y, EmiPort.literal(if (isExpanded) "-" else "+"))

        context.pop()
    }

    // -- Tooltip --

    override fun getTooltip(): List<ClientTooltipComponent> {
        val tooltips = mutableListOf<ClientTooltipComponent>()

        // Group name
        tooltips += ClientTooltipComponent.create(
            Component.literal(groupName).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW).visualOrderText
        )

        // Group ID in advanced mode
        if (EmiUtil.showAdvancedTooltips()) {
            tooltips += ClientTooltipComponent.create(
                EmiPort.literal(id.toString(), ChatFormatting.DARK_GRAY).visualOrderText
            )
        }

        // Mod name
        if (EmiConfig.appendModId) {
            tooltips += ClientTooltipComponent.create(
                EmiPort.literal(EmiUtil.getModName(id.namespace), ChatFormatting.BLUE, ChatFormatting.ITALIC).visualOrderText
            )
        }

        // Click hint
        tooltips += ClientTooltipComponent.create(
            Component.translatable(
                if (isExpanded) "emiplusplus.tooltip.collapse" else "emiplusplus.tooltip.expand"
            ).withStyle(ChatFormatting.GRAY).visualOrderText
        )

        // Member preview (inline icons)
        if (members.isNotEmpty()) {
            tooltips.add(TagTooltipComponent(members.map { it.realStack }))
        }

        return tooltips
    }

    // -- Equality --

    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = groupId.hashCode()
    override fun toString(): String = "EmiGroupStack[$groupId, expanded=$isExpanded, members=${members.size}]"

    companion object {
        const val ENTRY_SIZE = 18
    }
}
