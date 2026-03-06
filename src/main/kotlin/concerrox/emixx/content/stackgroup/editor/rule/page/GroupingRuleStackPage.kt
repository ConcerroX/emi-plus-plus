package concerrox.emixx.content.stackgroup.editor.rule.page

import com.lowdragmc.lowdraglib2.gui.ui.elements.Label
import concerrox.blueberry.ui.binding.liveData
import concerrox.blueberry.ui.lowdraglib2.Column
import concerrox.blueberry.ui.lowdraglib2.Label
import concerrox.blueberry.ui.lowdraglib2.Row
import concerrox.blueberry.ui.lowdraglib2.UIScope
import concerrox.blueberry.ui.lowdraglib2.util.flexFill
import concerrox.blueberry.ui.neo.OreSprites
import concerrox.blueberry.ui.neo.component.NeoTextField
import concerrox.blueberry.ui.neo.component.preference.NeoPreference
import concerrox.emixx.content.stackgroup.data.AbstractStackGroup
import concerrox.emixx.content.stackgroup.editor.component.StackPreview
import concerrox.emixx.content.stackgroup.editor.rule.GroupingRuleDialogViewModel
import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import concerrox.emixx.id
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import dev.vfyjxf.taffy.style.AlignItems
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

fun UIScope.GroupingRuleStackPage(viewModel: GroupingRuleDialogViewModel) = Column(
    layout = { paddingAll(1f) },
    styles = { background(OreSprites.PAGE_BACKGROUND) },
) {
    var pageCounter: Label? = null
    var pageCount = 0
    NeoPreference(layout = { paddingHorizontal(8f) }) {
        Row(layout = { alignItems(AlignItems.CENTER) }) {
            pageCounter = Label(CommonComponents.EMPTY, textStyles = { adaptiveWidth(true).textShadow(false) })
            NeoTextField(layout = { flexFill().marginLeft(6f) }).apply {
                textFieldStyle.placeholder(Component.literal("Search stacks…"))
            }
        }
    }.apply {
        titleElement.setDisplay(false)
    }
    NeoPreference(layout = { paddingHorizontal(8f) }) {
        val stacks = liveData { EmiStackList.filteredStacks }
        val sgs = object : AbstractStackGroup(id(""), "", false) {
            override fun match(stack: EmiStack) = TODO("Not yet implemented")
            override fun loadContent() = TODO("Not yet implemented")
        }

        StackPreview(
            data = stacks,
            rows = 12,
            onPickup = { result ->
                if (result !is EmiStack) return@StackPreview
                viewModel.previewStacks.value = EmiStackList.filteredStacks.filter {
                    it is EmiStack && it.id == result.id
                }
                viewModel.notation.value = "&item:${result.id}"
                stacks.value = EmiStackList.filteredStacks.map {
                    // TODO: wrapper w/o real groups
                    if (it is EmiStack && it.id == result.id) GroupedEmiStackWrapper(
                        it, sgs
                    ) else it
                }
            },
            onPageCountChange = { pageCount = it },
            onPageChange = { pageCounter?.setText("$it/$pageCount") },
        )
    }.apply {
        titleElement.setDisplay(false)
    }
}
