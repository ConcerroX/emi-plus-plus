package concerrox.emixx.content.stackgroup.data.upgrader

import com.google.gson.Gson
import com.google.gson.JsonElement
import concerrox.emixx.util.logInfo
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText

object LegacyStackGroupUpgrader {

    fun isLegacy(json: JsonElement) = json.asJsonObject.has("contents")

    fun upgrade(gson: Gson, json: JsonElement, path: Path): JsonElement {
        val obj = json.asJsonObject
        obj.add("includes", obj.remove("contents"))
        obj.add("excludes", obj.remove("exclusions"))
        path.deleteExisting()
        path.createFile().writeText(gson.toJson(obj))
        logInfo("Upgraded legacy stack group: ${path.fileName}")
        return obj
    }

}