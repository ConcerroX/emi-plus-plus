package concerrox.emixx.content.villagertrade

import concerrox.emixx.Identifier
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.npc.VillagerProfession

class VillagerTradeRecipe(
    profession: VillagerProfession,
    level: Int,
    private val inputs: MutableList<out EmiIngredient>,
    private val output: MutableList<EmiStack>,
) : EmiRecipe {

    private val title =
        Component.translatable("entity.minecraft.villager.${Identifier.parse(profession.name()).path}").append(" - ")
            .append(Component.translatable("merchant.level.$level"))

    override fun getCategory() = VillagerTradeManager.VILLAGER_TRADES
    override fun getId() = null
    override fun getInputs() = inputs
    override fun getOutputs() = output
    override fun getDisplayWidth() = 144
    override fun getDisplayHeight() = 28

    override fun addWidgets(widgets: WidgetHolder) {
        widgets.addTexture(EmiTexture.PLUS, 27, 13)
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 11)
        widgets.addText(title, 0, 0, 0xFFFFFF, true)
        widgets.addSlot(inputs[0], 0, 10)
        widgets.addSlot(inputs[1], 49, 10)
        widgets.addSlot(output[0], 107, 10).recipeContext(this)
        // TODO: add entity preview
    }

}