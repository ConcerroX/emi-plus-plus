package concerrox.emixx.content

import concerrox.emixx.content.stackgroup.StackGroups
import concerrox.emixx.mixin.emi.EmiReloadManagerAccessor
import concerrox.emixx.util.logInfo
import dev.emi.emi.runtime.EmiReloadLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReloadManager {

    private val reloadScope = CoroutineScope(Dispatchers.Default)

    fun reload() = try {
        StackGroups.reload()
    } catch (e: Exception) {
        EmiReloadLog.warn("Critical error occurred during EMI++ reload: ", e)
        EmiReloadManagerAccessor.setStatus(-1)
    }

    @JvmStatic
    fun reloadAsync() = reloadScope.launch {
        logInfo("Starting EMI++ reload in parallel…")
        reload()
    }

    @JvmStatic
    fun clear() {
        StackGroups.clear()
    }

}