package concerrox.emixx.config

import concerrox.emixx.EmiPlusPlus
import net.neoforged.fml.loading.FMLPaths
import kotlin.io.path.div

object EmiPlusPlusPaths {
    val CONFIG = FMLPaths.CONFIGDIR.get() / EmiPlusPlus.MOD_ID
    val STACK_GROUPS = CONFIG / "groups"
}