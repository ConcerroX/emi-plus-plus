package concerrox.minecraft.emiplusplus

import com.mojang.logging.LogUtils
import net.neoforged.fml.common.Mod
import org.slf4j.Logger

@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlus {
    companion object {
        const val MOD_ID = "emixx"
        val LOGGER: Logger = LogUtils.getLogger()
    }

    init {
        LOGGER.info("EMI++ initialized!")
    }
}
