package concerrox.emixx.content.stackgroup.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import concerrox.emixx.EmiPlusPlus
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import java.nio.file.Path

data class EmiStackGroupV2(
    val id: ResourceLocation,
    val name: Component,
    val contentGroupingRules: List<ContentGroupingRule>,
    val isEnabled: Boolean
) {

    companion object {

        fun parse(json: JsonElement, filename: Path): EmiStackGroupV2? {
            try {
                if (json !is JsonObject) throw IllegalArgumentException("Not a JSON object")

                if (!GsonHelper.isStringValue(json, "id")) {
                    throw IllegalArgumentException("ID is either not present or not a string")
                }
                val id = ResourceLocation.parse(json.get("id").asString)

                if (!GsonHelper.isArrayNode(json, "contents")) {
                    throw IllegalArgumentException("Contents are either not present or not a list")
                }

                val contents = mutableListOf<ContentGroupingRule>()
                for (element in json.getAsJsonArray("contents")) {
                    val split = element.asString.split(":")
                    val type = split[0]
                    contents += if (type.startsWith('#')) {
                        ContentGroupingRule.Tag(
                            type.substring(1), ResourceLocation.fromNamespaceAndPath(split[1], split[2])
                        )
                    } else if (type.startsWith("*")) {
                        ContentGroupingRule.Regex(type.substring(1), Regex(element.asString.removePrefix("$type:")))
                    } else {
                        ContentGroupingRule.Stack(type, element)
                    }
                }

                val name = if (GsonHelper.isStringValue(json, "name")) {
                    Component.translatable(json.get("name").asString)
                } else {
                    Component.translatable("stackgroup.${id.namespace}.${id.path.replace('/', '.')}")
                }

                return EmiStackGroupV2(id, name, contents, GsonHelper.getAsBoolean(json, "enabled", false))
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to parse {}: {}", filename, e)
                return null
            }
        }

    }

    // Collect this by StackGroupContentCollector
    var collectedStacks = mutableListOf<EmiStack>()

    sealed class ContentGroupingRule {
        data class Tag(val type: String, val tag: ResourceLocation) : ContentGroupingRule()
        data class Stack(val type: String, val stack: JsonElement) : ContentGroupingRule()
        data class Regex(val type: String, val expression: kotlin.text.Regex) : ContentGroupingRule()
    }

}