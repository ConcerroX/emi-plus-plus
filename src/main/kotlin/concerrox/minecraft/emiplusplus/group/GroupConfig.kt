package concerrox.minecraft.emiplusplus.group

import com.mojang.logging.LogUtils
import concerrox.minecraft.emiplusplus.Identifier
import kotlinx.serialization.Serializable
import org.slf4j.Logger

/**
 * Deserialized JSON config for one group.
 *
 * Example:
 * ```json
 * {
 *     "name": "Boats",
 *     "id": "minecraft:boats",
 *     "description": "Boats can be used to travel across water.",
 *     "includes": [
 *         "item:minecraft:boat",
 *         "item:minecraft:chest_boat",
 *         "#item:minecraft:planks"
 *     ]
 * }
 * ```
 */
@Serializable
data class GroupConfig(
    val name: String,
    val id: String,
    val description: String = "",
    val includes: List<String> = emptyList()
) {
    companion object {
        val LOGGER: Logger = LogUtils.getLogger()

        fun parseSelectors(config: GroupConfig): List<GroupSelector> {
            return config.includes.mapNotNull { notation ->
                GroupSelector.parse(notation) ?: run {
                    LOGGER.warn("Invalid group selector '{}' in group '{}'", notation, config.id)
                    null
                }
            }
        }
    }

    val resourceId: Identifier?
        get() = Identifier.tryParse(id)
}
