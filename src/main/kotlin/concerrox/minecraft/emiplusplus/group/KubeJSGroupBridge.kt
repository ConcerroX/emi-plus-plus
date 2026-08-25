package concerrox.minecraft.emiplusplus.group

import dev.latvian.mods.kubejs.recipe.viewer.server.FluidData
import dev.latvian.mods.kubejs.recipe.viewer.server.ItemData
import dev.latvian.mods.kubejs.recipe.viewer.server.RecipeViewerData
import dev.latvian.mods.kubejs.recipe.viewer.server.RemoteRecipeViewerDataUpdatedEvent
import net.neoforged.neoforge.common.NeoForge

/**
 * Bridges KubeJS's `RecipeViewerEvents.groupEntries` data into EMI++'s group system.
 * KubeJS syncs grouped-entry data to the client and fires [RemoteRecipeViewerDataUpdatedEvent]
 * on the NeoForge event bus whenever it changes; we cache the latest payload here and
 * re-bake EMI++'s groups when it updates.
 *
 * Only call [register] if the `kubejs` mod is actually loaded — see [concerrox.minecraft.emiplusplus.EmiPlusPlus].
 */
object KubeJSGroupBridge {

    @Volatile
    private var remote: RecipeViewerData? = null

    fun register() {
        NeoForge.EVENT_BUS.addListener(::onRemoteData)
    }

    private fun onRemoteData(event: RemoteRecipeViewerDataUpdatedEvent) {
        remote = event.data
        StackGroups.bakeOnly()
    }

    fun itemGroups(): List<ItemData.Group> = remote?.itemData()?.groupedEntries().orEmpty()

    fun fluidGroups(): List<FluidData.Group> = remote?.fluidData()?.groupedEntries().orEmpty()
}