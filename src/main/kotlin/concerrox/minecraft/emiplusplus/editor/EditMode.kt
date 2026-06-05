package concerrox.minecraft.emiplusplus.editor

sealed class EditMode {
    data object NONE : EditMode()
    data class AddById(val groupId: String) : EditMode()
    data class AddByTag(val groupId: String) : EditMode()
}
