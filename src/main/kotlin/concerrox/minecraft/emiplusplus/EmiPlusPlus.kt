package concerrox.minecraft.emiplusplus

import com.mojang.logging.LogUtils
import concerrox.minecraft.emiplusplus.group.KubeJSGroupBridge
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.LoadingModList
import org.slf4j.Logger

@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlus {
    companion object {
        const val MOD_ID = "emixx"
        val LOGGER: Logger = LogUtils.getLogger()
    }

    init {
        LOGGER.info("EMI++ initialized!")
        if (LoadingModList.get().getModFileById("kubejs") != null) {
            KubeJSGroupBridge.register()
        }
    }
}
