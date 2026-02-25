package concerrox.emixx.content.stackgroup.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.Identifier
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.registry.EmiStackList
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path
import java.util.*

class EmiStackGroupV2(
    id: ResourceLocation,
    name: String, // TODO: use name key instead
    isEnabled: Boolean,
    val rules: List<GroupingRule>,
) : AbstractStackGroup(id, name, isEnabled) {

    companion object {

        val CODEC: Codec<EmiStackGroupV2> = RecordCodecBuilder.create { builder ->
            builder.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(EmiStackGroupV2::id),
                Codec.STRING.optionalFieldOf("name").forGetter { Optional.of(it.name) },
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(EmiStackGroupV2::isEnabled),
                GroupingRule.CODEC.listOf().fieldOf("includes").forGetter(EmiStackGroupV2::rules)
            ).apply(builder) { id, nameOptional, enabled, rules ->
                val name = nameOptional.orElse("stackgroup.${id.namespace}.${id.path.replace('/', '.')}")
                EmiStackGroupV2(id, name, enabled, rules)
            }
        }

        @Deprecated("Use CODEC instead")
        @ApiStatus.ScheduledForRemoval
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

                val content = mutableListOf<GroupingRule>()
                for (element in json.getAsJsonArray("contents")) {
                    val split = element.asString.split(":")
                    val type = split[0]
                    content += if (type.startsWith('#')) {
                        GroupingRule.Tag(
                            type.substring(1), Identifier.fromNamespaceAndPath(split[1], split[2])
                        )
                    } else if (type.startsWith("*")) {
                        GroupingRule.Regex(Regex(element.asString.removePrefix("$type:")))
                    } else {
                        GroupingRule.Stack(element)
                    }
                }

                val name = if (GsonHelper.isStringValue(json, "name")) {
                    json.get("name").asString
                } else {
                    "stackgroup.${id.namespace}.${id.path.replace('/', '.')}"
                }

                return EmiStackGroupV2(id, name, GsonHelper.getAsBoolean(json, "enabled", false), content)
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to parse {}: {}", filename, e)
                return null
            }
        }

    }

    @Deprecated("bake a grouper")
    @ApiStatus.ScheduledForRemoval
    var collectedStacks = mutableListOf<EmiStack>()

    override fun toString(): String {
        return "EmiStackGroupV2(id=$id, name=$name, isEnabled=$isEnabled, rules=$rules)"
    }

    override fun match(stack: EmiStack): Boolean {
        for (rule in rules) if (rule.match(stack)) return true
        return false
    }

    override fun loadContent(): List<EmiStack> {
        val ret = mutableListOf<EmiStack>()
        for (stack in EmiStackList.filteredStacks) if (match(stack)) ret += stack
        return ret
    }

}