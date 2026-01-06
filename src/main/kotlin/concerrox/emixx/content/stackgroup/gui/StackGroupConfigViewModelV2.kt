package concerrox.emixx.content.stackgroup.gui

import concerrox.emixx.content.stackgroup.data.EmiStackGroupV2
import concerrox.emixx.oreui.view.ViewModel
import concerrox.emixx.oreui.view.liveData

class StackGroupConfigViewModelV2 : ViewModel {

    val prebuiltStackGroups = liveData { emptyList<EmiStackGroupV2>() }
    val customizedStackGroups = liveData { emptyList<EmiStackGroupV2>() }

    fun load() {
    }

}