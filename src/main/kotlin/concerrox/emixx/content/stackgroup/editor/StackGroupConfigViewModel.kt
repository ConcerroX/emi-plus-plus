package concerrox.emixx.content.stackgroup.editor

import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.content.stackgroup.EmiPlusPlusStackGroups
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.content.stackgroup.editor.component.StackPreview
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component

class StackGroupConfigViewModel : ViewModel() {

    private val loadScope = CoroutineScope(Dispatchers.IO)
    lateinit var editorSharedData: EditorSharedData
    val stackGroups = liveData { emptyList<StackGroupUiState>() }

    fun loadStackGroups() = loadScope.launch {
        loadEditorSharedData()
        stackGroups.value = EmiPlusPlusStackGroups.stackGroups.map {
            StackGroupUiState(
                it,
                Component.translatable(it.name),
                Component.literal(it.id.toString()),
                it.loadContent().take(StackPreview.PREVIEW_STACKS_COUNT)
            )
        }
    }

    @Suppress("UnstableApiUsage")
    fun loadEditorSharedData() {
        editorSharedData = EditorSharedData(
            stackRegistries = EmiTags.ADAPTERS_BY_REGISTRY.keys.toList(),
        )
    }

    data class StackGroupUiState(
        val stackGroup: EmiStackGroupV2,
        val nameComponent: Component,
        val idComponent: Component,
        val previewStacks: List<EmiStack>
    )

    data class EditorSharedData(
        val stackRegistries: List<Registry<*>?>,
    )

}