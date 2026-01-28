package concerrox.emixx.config

import com.electronwill.nightconfig.core.AbstractConfig
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import concerrox.blueberry.ui.screen.ScreenManager
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.Minecraft
import concerrox.emixx.content.stackgroup.editor.StackGroupConfigScreen
import concerrox.emixx.registry.ModTranslationKeys
import concerrox.emixx.util.text
import dev.emi.emi.EmiPort
import dev.emi.emi.config.ConfigEnum
import dev.emi.emi.screen.ConfigScreen
import dev.emi.emi.screen.widget.config.*
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.neoforged.neoforge.common.ModConfigSpec


object EmiConfigScreenAttachment {

    @JvmField
    val EMI_PLUS_PLUS_GROUP_NAME: Component = EmiPort.literal("E", Style.EMPTY.withColor(0xEB7BFC))
        .append(EmiPort.literal("M", Style.EMPTY.withColor(0x7BFCA2)))
        .append(EmiPort.literal("I", Style.EMPTY.withColor(0x7BEBFC)))
        .append(EmiPort.literal("++", Style.EMPTY.withColor(ChatFormatting.WHITE)))

    private const val MOD_ID = EmiPlusPlus.MOD_ID
    private const val TRANSLATION_KEY_PREFIX = "$MOD_ID.configuration"

    private val backupOldConfigs = mutableMapOf<ModConfigSpec.ConfigValue<out Any?>, Any?>()
    private var screen: ConfigScreen? = null
    private var searchSupplier: (() -> String)? = null
    private var configSpec: ModConfigSpec? = null

    @JvmStatic
    fun attachConfigList(screen: ConfigScreen, listView: ListWidget, searcher: ConfigSearch) {
        this.screen = screen
        backupOldConfigs.clear()
        searchSupplier = { searcher.search }
        configSpec = EmiPlusPlusConfig.CONFIG_SPEC

        val rootWidget = GroupNameWidget(MOD_ID, EMI_PLUS_PLUS_GROUP_NAME)
        listView.addEntry(rootWidget)
//        if (collapsed.contains(text.getString())) {
//            lastGroupWidget.collapsed = true;

        for (groupEntry in requireNotNull(configSpec).values.entrySet()) {
            val groupKey = groupEntry.key
            val group = groupEntry.getValue<AbstractConfig>()
            val groupId = "$MOD_ID.$groupKey"
            val groupWidget = SubGroupNameWidget(groupId, text("$TRANSLATION_KEY_PREFIX.$groupKey"))

//        if (collapsed.contains(text.getString())) {
//            currentSubGroupWidget.collapsed = true

            groupWidget.parent = rootWidget
            listView.addEntry(groupWidget)

            if (groupKey == "stackGroups") {
                val configKey = "stackGroupConfig"
                val configId = "$groupId.$configKey"
                val configWidget = object : ConfigEntryWidget(
                    text("$TRANSLATION_KEY_PREFIX.$configKey"), listOf(), searchSupplier, 20
                ) {

                    private val button = EmiPort.newButton(
                        0, 0, 150, 20, ModTranslationKeys.Config.STACK_GROUP_CONFIG_EDIT.asComponent()
                    ) {
                        ScreenManager.pushScreen(StackGroupConfigScreen())
//                        val root = UIElement()
//                        Minecraft.setScreen(ModularUIScreen(ModularUI.of(UI.of(root)), Component.empty()))
//                        root.clearAllChildren()
//                        root.addChild(UIElement())
                    }

                    init {
                        setChildren(listOf(button))
                    }

                    override fun update(y: Int, x: Int, width: Int, height: Int) {
                        button.x = x + width - button.getWidth()
                        button.y = y
                    }

                }

                listView.addEntry(configWidget)
                configWidget.endGroup = false

                rootWidget.children.add(configWidget)
                configWidget.parentGroups.add(rootWidget)

                groupWidget.children.add(configWidget)
                configWidget.parentGroups.add(groupWidget)
            }

            for (configEntry in group.entrySet()) {
                val configKey = configEntry.key
                val config = configEntry.getValue<ModConfigSpec.ConfigValue<Any?>>()
                val configId = "$groupId.$configKey"
                val configWidget = createConfigWidget(config, text("$TRANSLATION_KEY_PREFIX.$configKey"))

                listView.addEntry(configWidget)
                configWidget.endGroup = configEntry == group.entrySet().last()

                rootWidget.children.add(configWidget)
                configWidget.parentGroups.add(rootWidget)

                groupWidget.children.add(configWidget)
                configWidget.parentGroups.add(groupWidget)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createConfigWidget(
        config: ModConfigSpec.ConfigValue<Any?>, name: Component,
    ): ConfigEntryWidget {
        val search = requireNotNull(searchSupplier)
        return when (config) {
            is ModConfigSpec.BooleanValue -> BooleanWidget(name, listOf(), search, createMutator(config))
            is ModConfigSpec.IntValue -> IntWidget(name, listOf(), search, createMutator(config))
            is ModConfigSpec.EnumValue -> EnumWidget(
                name, listOf(), search, createMutator(config as ModConfigSpec.EnumValue<ConfigEnum>)
            ) { true }

            // TODO: fix this
            is ModConfigSpec.ConfigValue -> BooleanWidget(name, listOf(), search, screen?.run {
                object : ConfigScreen.Mutator<Boolean>() {
                    override fun getValue() = true
                    override fun setValue(value: Boolean) {
                        backupOldConfigs[config] = config.get()
                        config.set(requireNotNull(value))
                        requireNotNull(configSpec).save()
                    }
                }
            })

            else -> throw UnsupportedOperationException("Unsupported config type: ${config::class.java}")
        }

    }

    private fun <T> createMutator(config: ModConfigSpec.ConfigValue<T>): ConfigScreen.Mutator<T> {
        return requireNotNull(screen).run {
            object : ConfigScreen.Mutator<T>() {
                override fun getValue() = config.get()
                override fun setValue(value: T) {
                    backupOldConfigs[config] = config.get()
                    config.set(requireNotNull(value))
                    requireNotNull(configSpec).save()
                }
            }
        }
    }

}