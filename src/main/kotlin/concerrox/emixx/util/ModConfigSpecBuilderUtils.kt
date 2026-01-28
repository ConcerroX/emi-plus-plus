package concerrox.emixx.util

import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.reflect.KMutableProperty0

// TODO: migrate to BlueberryLib

fun ModConfigSpec.Builder.group(path: String, action: ModConfigSpec.Builder.() -> Unit) = apply {
    push(path)
    action(this)
    pop()
}

fun <V : Enum<V>> ModConfigSpec.Builder.enum(
    property: KMutableProperty0<ModConfigSpec.EnumValue<V>>, defaultValue: V
) {
    property.set(defineEnum(property.name, defaultValue))
}

fun ModConfigSpec.Builder.integer(
    property: KMutableProperty0<ModConfigSpec.IntValue>, defaultValue: Int, range: IntRange
) {
    property.set(defineInRange(property.name, defaultValue, range.min(), range.max()))
}

fun ModConfigSpec.Builder.boolean(
    property: KMutableProperty0<ModConfigSpec.BooleanValue>, defaultValue: Boolean
) {
    property.set(define(property.name, defaultValue))
}