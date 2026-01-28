package concerrox.emixx.content.stackgroup.editor

import concerrox.blueberry.ui.binding.ViewModel
import concerrox.blueberry.ui.binding.liveData
import concerrox.emixx.content.stackgroup.StackGroupManagerV2

class StackGroupConfigViewModel : ViewModel {

    private val onLoadingStatusChangedListener = { v: StackGroupManagerV2.LoadingStatus -> loadingStatus.value = v }
    val loadingStatus = liveData { StackGroupManagerV2.loadingStatus }

    val data = liveData { listOf(1) }

    init {
        StackGroupManagerV2.addOnLoadingStatusChangedListener(onLoadingStatusChangedListener)
    }

    override fun onDetached() {
        StackGroupManagerV2.removeOnLoadingStatusChangedListener(onLoadingStatusChangedListener)
    }

}