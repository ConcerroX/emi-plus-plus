package concerrox.emixx.util

inline fun <T, R> Iterable<T>.mutableMap(
    transform: (T) -> R
): MutableList<R> {
    return mapTo(ArrayList(collectionSizeOrDefault(10)), transform)
}

fun <T> Iterable<T>.collectionSizeOrDefault(default: Int) = if (this is Collection<*>) size else default