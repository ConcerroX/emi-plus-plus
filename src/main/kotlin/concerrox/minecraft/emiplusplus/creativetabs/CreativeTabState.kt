package concerrox.minecraft.emiplusplus.creativetabs

data class CreativeTabState(
    val tabs: List<CreativeTabEntry> = emptyList(),
    val currentTabPage: Int = 0,
    val selectedTab: CreativeTabEntry? = null,
)
