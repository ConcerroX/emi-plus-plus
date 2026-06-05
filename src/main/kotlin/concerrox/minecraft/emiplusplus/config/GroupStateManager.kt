package concerrox.minecraft.emiplusplus.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.client.Minecraft
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Per-world persistence of which groups are expanded.
 *
 * Saved to `<gameDir>/config/emi-plus-plus/group_state.json`
 * as a simple JSON object mapping group ID → boolean.
 */
object GroupStateManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val state: MutableMap<String, Boolean> = mutableMapOf()

    fun isExpanded(groupId: String): Boolean = state[groupId] ?: false

    fun setExpanded(groupId: String, expanded: Boolean) {
        state[groupId] = expanded
        save()
    }

    fun toggle(groupId: String) {
        setExpanded(groupId, !isExpanded(groupId))
    }

    fun load() {
        state.clear()
        val file = getStateFile()
        if (!file.exists()) return

        try {
            val json = file.readText()
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            val loaded: Map<String, Boolean> = gson.fromJson(json, type)
            state.putAll(loaded)
        } catch (_: Exception) {
            // Corrupted state file — start fresh
        }
    }

    fun save() {
        try {
            val file = getStateFile()
            file.parent.createDirectories()
            file.writeText(gson.toJson(state))
        } catch (_: Exception) {
            // Can't save — silently ignore
        }
    }

    private fun getStateFile(): Path {
        val gameDir = Minecraft.getInstance().gameDirectory.toPath()
        return gameDir.resolve("config/emixx/group_state.json")
    }
}
