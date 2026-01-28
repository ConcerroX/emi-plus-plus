package concerrox.emixx.content.stackgroup.ui

import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2

class StackGroupConfigViewModelV2 : ViewModel {

    val prebuiltStackGroups = liveData { emptyList<EmiStackGroupV2>() }
    val customizedStackGroups = liveData { emptyList<EmiStackGroupV2>() }

    fun load() {
    }

    override fun onDetached() {
    }

}