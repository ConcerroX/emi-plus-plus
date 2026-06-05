package concerrox.minecraft.emiplusplus.group

import com.mojang.logging.LogUtils
import concerrox.minecraft.emiplusplus.config.EmiPlusPlusConfig
import concerrox.minecraft.emiplusplus.config.GroupStateManager
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.config.SidebarType
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.screen.EmiScreenManager
import kotlinx.serialization.json.Json
import net.minecraft.client.Minecraft
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Central manager for collapsible item groups.
 */
object StackGroups {

    val LOGGER: Logger = LogUtils.getLogger()

    private val json = Json { ignoreUnknownKeys = true }

    val groups: MutableList<GroupConfig> = mutableListOf()
    var assembler: GroupAssembler? = null
        private set

    /** The current transformed index stack list (grouped). */
    @Volatile
    var indexStacks: List<EmiStack> = EmiStackList.stacks
        private set

    /**
     * Dirty flag set when indexStacks changes and EmiSearch.stacks needs syncing.
     * EmiScreenManagerMixin reads and clears this before each EMI render frame.
     */
    @Volatile
    var needsSync: Boolean = false

    /**
     * Called synchronously from EmiReloadWorkerMixin after EMI finishes reloading.
     * Runs on EMI's reload thread; heavy work happens here.
     */
    fun reload() {
        if (!EmiPlusPlusConfig.stackGroupsEnabled) {
            LOGGER.info("Stack groups disabled, skipping reload")
            assembler = null
            indexStacks = EmiStackList.stacks
            return
        }

        LOGGER.info("EMI++ reloading stack groups...")
        load()
        bake()

        // Schedule UI refresh on the render thread
        val mc = Minecraft.getInstance()
        mc.execute {
            EmiScreenManager.repopulatePanels(SidebarType.INDEX)
        }
    }

    // -- Loading --

    private fun load() {
        groups.clear()
        val dir = getGroupsDir()
        if (!dir.exists()) {
            LOGGER.debug("No groups directory found at {}", dir)
            return
        }

        dir.listDirectoryEntries("*.json").forEach { file ->
            try {
                val config = json.decodeFromString<GroupConfig>(file.readText())
                groups += config
                LOGGER.debug("Loaded group: {}", config.id)
            } catch (e: Exception) {
                LOGGER.warn("Failed to parse group config {}: {}", file.fileName, e.message)
            }
        }
        LOGGER.info("Loaded {} group configs", groups.size)
    }

    // -- Baking --

    private fun bake() {
        GroupStateManager.load()

        val selectors = mutableMapOf<String, List<GroupSelector>>()
        for (group in groups) {
            val parsed = GroupConfig.parseSelectors(group)
            if (parsed.isNotEmpty()) {
                selectors[group.id] = parsed
            }
        }

        assembler = GroupAssembler(groups, selectors)
        indexStacks = assembler!!.buildIndexStacks()
        needsSync = true

        // Apply persisted expand state
        for (stack in indexStacks) {
            if (stack is EmiGroupStack && GroupStateManager.isExpanded(stack.groupId)) {
                expand(stack)
            }
        }

        LOGGER.info(
            "Baked {} groups with {} selectors, {} total stacks",
            groups.size,
            selectors.values.sumOf { it.size },
            indexStacks.size
        )
    }

    // -- Expand / Collapse --

    fun expand(groupStack: EmiGroupStack) {
        val newList = indexStacks.toMutableList()
        val pos = newList.indexOf(groupStack)
        if (pos < 0) return

        newList.addAll(pos + 1, groupStack.members.toList())
        groupStack.isExpanded = true
        indexStacks = newList
        needsSync = true
        GroupStateManager.setExpanded(groupStack.groupId, true)
        EmiScreenManager.repopulatePanels(SidebarType.INDEX)
    }

    fun collapse(groupStack: EmiGroupStack) {
        val newList = indexStacks.toMutableList()
        newList.removeAll { it is GroupedEmiStackWrapper && it.groupStack === groupStack }
        groupStack.isExpanded = false
        indexStacks = newList
        needsSync = true
        GroupStateManager.setExpanded(groupStack.groupId, false)
        EmiScreenManager.repopulatePanels(SidebarType.INDEX)
    }

    /**
     * Sync our grouped list to EmiSearch.stacks so the search panel
     * renders the grouped version. Only syncs when no search query is active.
     */
    /** Expand the group with given ID in the EMI sidebar (if it exists) */
    fun expandById(groupId: String) {
        for (stack in indexStacks) {
            if (stack is EmiGroupStack && stack.groupId == groupId && !stack.isExpanded) {
                expand(stack)
                return
            }
        }
    }

    fun toggle(groupStack: EmiGroupStack) {
        if (groupStack.isExpanded) collapse(groupStack) else expand(groupStack)
    }

    fun afterSearchedStacks(stacks: List<EmiStack>): List<EmiStack> {
        return assembler?.search(stacks) ?: stacks
    }

    /** Save all in-memory groups back to disk. */
    /** Re-bake without reloading from disk. Used after in-memory edits. */
    fun bakeOnly() {
        bake()
        needsSync = true
        val mc = Minecraft.getInstance()
        mc.execute { EmiScreenManager.repopulatePanels(SidebarType.INDEX) }
    }

    fun groupsDir() = getGroupsDir()

    fun saveAll() {
        val dir = getGroupsDir()
        dir.createDirectories()
        for (group in groups) {
            try {
                val file = dir.resolve(group.configFilename)
                file.writeText(json.encodeToString(GroupConfig.serializer(), group))
            } catch (e: Exception) {
                LOGGER.warn("Failed to save group {}: {}", group.id, e.message)
            }
        }
    }

    /** Derived filename for a group config. */
    private val GroupConfig.configFilename: String
        get() = id.replace(":", "__").replace("/", "__") + ".json"

    // -- Directory --

    private fun getGroupsDir(): Path {
        val gameDir = Minecraft.getInstance().gameDirectory.toPath()
        return gameDir.resolve("config/emixx/stack_groups")
    }
}
