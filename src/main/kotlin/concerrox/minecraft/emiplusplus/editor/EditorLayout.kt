package concerrox.minecraft.emiplusplus.editor

import concerrox.minecraft.emiplusplus.group.GroupConfig
import concerrox.minecraft.emiplusplus.group.StackGroups

/** RecipeScreen-style: pack groups into pages by height */
internal fun StackGroupEditorScreen.bakePages() {
    val maxHeight = backgroundHeight - 4
    val pages = mutableListOf<MutableList<GroupConfig>>()
    var current = mutableListOf<GroupConfig>()
    var heightUsed = 0

    for (group in StackGroups.groups) {
        val cardH = cardHeight(group.includes.size)

        if (current.isNotEmpty() && heightUsed + cardH > maxHeight) {
            pages.add(current)
            current = mutableListOf()
            heightUsed = 0
        }
        heightUsed += cardH
        current.add(group)
    }
    if (current.isNotEmpty()) pages.add(current)
    this.pages = pages
    if (currentPage >= pages.size) currentPage = maxOf(0, pages.size - 1)
}

internal fun cardHeight(selectorCount: Int): Int = 32 + minOf(selectorCount, 6) * 18

/** Find which group card is at the given screen position */
internal fun StackGroupEditorScreen.findGroupAtPos(mx: Int, my: Int): GroupConfig? {
    var cardY = panelY + 19
    val visibleGroups = pages.getOrElse(currentPage) { emptyList() }
    for (group in visibleGroups) {
        val height = cardHeight(group.includes.size)
        if (mx in (panelX + 5)..(panelX + backgroundWidth - 5) && my in cardY..(cardY + height)) {
            return group
        }
        cardY += height
    }
    return null
}
