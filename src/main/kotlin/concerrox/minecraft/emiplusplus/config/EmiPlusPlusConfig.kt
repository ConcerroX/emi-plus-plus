package concerrox.minecraft.emiplusplus.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.logging.LogUtils
import net.minecraft.client.Minecraft
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.*

/**
 * EMI++ configuration persisted as JSON in config/emixx/config.json.
 */
object EmiPlusPlusConfig {

    private val LOGGER: Logger = LogUtils.getLogger()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val data: MutableMap<String, Any> = mutableMapOf()

    var stackGroupsEnabled: Boolean
        get() = data["stack_groups_enabled"] as? Boolean ?: true
        set(value) {
            data["stack_groups_enabled"] = value
            save()
        }

    var showGroupBorder: Boolean
        get() = data["show_group_border"] as? Boolean ?: true
        set(value) {
            data["show_group_border"] = value
            save()
        }

    var showGroupFill: Boolean
        get() = data["show_group_fill"] as? Boolean ?: true
        set(value) {
            data["show_group_fill"] = value
            save()
        }

    var showGroupMemberTooltip: Boolean
        get() = data["show_member_tooltip"] as? Boolean ?: true
        set(value) {
            data["show_member_tooltip"] = value
            save()
        }

    fun load() {
        val file = getConfigFile()
        if (!file.exists()) return

        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val loaded: Map<String, Any> = gson.fromJson(file.readText(), type)
            data.clear()
            data.putAll(loaded)
        } catch (e: Exception) {
            LOGGER.warn("Failed to load config, using defaults", e)
        }
    }

    fun save() {
        try {
            val file = getConfigFile()
            file.parent.createDirectories()
            file.writeText(gson.toJson(data))
        } catch (e: Exception) {
            LOGGER.warn("Failed to save config", e)
        }
    }

    private fun getConfigFile(): Path {
        val gameDir = Minecraft.getInstance().gameDirectory.toPath()
        return gameDir.resolve("config/emixx/config.json")
    }

    init {
        load()
    }
}
