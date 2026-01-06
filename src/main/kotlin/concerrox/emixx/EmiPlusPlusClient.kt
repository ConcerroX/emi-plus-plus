package concerrox.emixx

import concerrox.emixx.config.EmiPlusPlusConfig
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@OnlyIn(Dist.CLIENT)
@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlusClient(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        modContainer.registerConfig(ModConfig.Type.CLIENT, EmiPlusPlusConfig.CONFIG_SPEC, "emixx/emixx-client.toml")
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java, IConfigScreenFactory(::ConfigurationScreen)
        )
    }

}