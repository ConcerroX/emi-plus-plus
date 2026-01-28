package concerrox.emixx.content.headerfooter

import com.lowdragmc.lowdraglib2.math.Rect
import concerrox.blueberry.ui.lowdraglib2.UIScope

abstract class HeaderFooter(val isHeader: Boolean) {

    abstract val contentHeight: Int
    abstract val gap: Int

    val visualHeight get() = contentHeight + gap

    abstract fun createUI(scope: UIScope, bounds: Rect)

    class NoneHeaderFooter(isHeader: Boolean) : HeaderFooter(isHeader) {
        override val contentHeight: Int get() = 0
        override val gap: Int get() = 0
        override fun createUI(scope: UIScope, bounds: Rect) {}
    }

    class SearchHeaderFooter(isHeader: Boolean) : HeaderFooter(isHeader) {
        override val contentHeight: Int get() = 0
        override val gap: Int get() = 0
        override fun createUI(scope: UIScope, bounds: Rect) {}
    }

}