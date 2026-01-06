package concerrox.emixx.util

import dev.emi.emi.runtime.EmiDrawContext

fun EmiDrawContext.push(action: EmiDrawContext.() -> Unit) = apply {
    push()
    action(this)
    pop()
}