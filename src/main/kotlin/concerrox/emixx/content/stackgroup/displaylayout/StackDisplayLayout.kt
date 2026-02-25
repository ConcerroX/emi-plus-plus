package concerrox.emixx.content.stackgroup.displaylayout

import concerrox.emixx.content.stackgroup.stack.GroupedEmiStackWrapper
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.gui.GuiGraphics
import kotlin.experimental.or

class StackDisplayLayout {

    companion object {
        private const val MAX_TILE_COUNT_PER_STACK = 3
    }

    private lateinit var stackGrid: Array<Array<EmiStack?>>
    private lateinit var tiles: Array<Tile?>

    private var lastTileIndex = -1
    private var isTilesDirty = true
    private var columnCount = 0
    private var rowCount = 0

    fun recreateLayout(columns: Int, rows: Int) {
        columnCount = columns
        rowCount = rows

        stackGrid = Array(rows) { Array(columns) { null } }
        isTilesDirty = true
    }

    fun putStack(x: Int, y: Int, stack: EmiStack) {
        stackGrid[y][x] = stack
    }

    fun collectDisplayStackList() {
    }

    fun render(guiGraphics: GuiGraphics, x: Int, y: Int) {
        if (isTilesDirty) {
            recreateTiles()
            isTilesDirty = false
        }

        var i = 0
        while (i < tiles.size && tiles[i] != null) {
            val tile = tiles[i]!!
            tile.render(guiGraphics, x, y)
            i++
        }
    }

    fun addTile(tile: Tile) {
        lastTileIndex++
        tiles[lastTileIndex] = tile
    }

    private fun recreateTiles() {
        tiles = Array(columnCount * rowCount * MAX_TILE_COUNT_PER_STACK) { null }
        lastTileIndex = -1

        for (tileY in stackGrid.indices) {
            for (tileX in stackGrid[tileY].indices) {
                val stack = stackGrid[tileY][tileX]
                if (stack is GroupedEmiStackWrapper<*>) createBorderTile(tileX, tileY, stack)
                // TODO: Selected
            }
        }
    }

    private fun createBorderTile(x: Int, y: Int, stack: GroupedEmiStackWrapper<*>) {
        var borderParts: Short = 0

        if (!stack.isInSameGroup(x - 1, y)) borderParts = borderParts or BorderTile.BorderPart.LEFT.bit
        if (!stack.isInSameGroup(x + 1, y)) borderParts = borderParts or BorderTile.BorderPart.RIGHT.bit
        if (!stack.isInSameGroup(x, y - 1)) borderParts = borderParts or BorderTile.BorderPart.TOP.bit
        if (!stack.isInSameGroup(x, y + 1)) borderParts = borderParts or BorderTile.BorderPart.BOTTOM.bit

        // Inner corners
        if (!stack.isInSameGroup(x - 1, y - 1) && stack.isInSameGroup(x - 1, y) && stack.isInSameGroup(x, y - 1)) {
            borderParts = borderParts or BorderTile.BorderPart.TOP_LEFT.bit
        }
        if (!stack.isInSameGroup(x + 1, y - 1) && stack.isInSameGroup(x + 1, y) && stack.isInSameGroup(x, y - 1)) {
            borderParts = borderParts or BorderTile.BorderPart.TOP_RIGHT.bit
        }
        if (!stack.isInSameGroup(x - 1, y + 1) && stack.isInSameGroup(x - 1, y) && stack.isInSameGroup(x, y + 1)) {
            borderParts = borderParts or BorderTile.BorderPart.BOTTOM_LEFT.bit
        }
        if (!stack.isInSameGroup(x + 1, y + 1) && stack.isInSameGroup(x + 1, y) && stack.isInSameGroup(x, y + 1)) {
            borderParts = borderParts or BorderTile.BorderPart.BOTTOM_RIGHT.bit
        }

        if (borderParts != 0.toShort()) addTile(BorderTile(x, y, borderParts))
    }

    private fun GroupedEmiStackWrapper<*>.isInSameGroup(x: Int, y: Int): Boolean {
        if (x !in 0..<columnCount) return false
        if (y !in 0..<rowCount) return false

        val other = stackGrid[y][x] as? GroupedEmiStackWrapper<*> ?: return false
        return other.stackGroup == stackGroup
    }

    abstract class Tile(val tileX: Int, val tileY: Int) {
        abstract fun render(guiGraphics: GuiGraphics, x: Int, y: Int)
    }

}