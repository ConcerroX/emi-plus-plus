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
 *         "#item:minecraft:planks"
 *     ],
 *     "color": "#66FF5733"
 * }
 * ```
 *
 * Color format: `#RRGGBB` or `#AARRGGBB`. Defaults to `#66FFFFFF` (semi-transparent white).
 */
@Serializable
data class GroupConfig(
    val name: String,
    val id: String,
    val description: String = "",
    val includes: List<String> = emptyList(),
    val color: String? = null
) {
    companion object {
        val LOGGER: Logger = LogUtils.getLogger()
        const val DEFAULT_BORDER_COLOR: Int = 0x66FFFFFF.toInt()

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

    /**
     * Parsed border color as ARGB int. Falls back to [DEFAULT_BORDER_COLOR].
     */
    val borderColor: Int by lazy { parseColor(color) }

    private fun parseColor(hex: String?): Int {
        if (hex == null) return DEFAULT_BORDER_COLOR
        try {
            val stripped = hex.removePrefix("#")
            return when (stripped.length) {
                6 -> (0x66000000 or stripped.toLong(16).toInt())  // RRGGBB -> 0x66RRGGBB
                8 -> stripped.toLong(16).toInt()                    // AARRGGBB
                else -> DEFAULT_BORDER_COLOR
            }
        } catch (_: Exception) {
            LOGGER.warn("Invalid color '{}' in group '{}', using default", hex, id)
            return DEFAULT_BORDER_COLOR
        }
    }
}
