@file:JvmName("LogUtils")

package concerrox.emixx.util

import net.neoforged.fml.loading.FMLEnvironment
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

private val isDebugMode = !FMLEnvironment.production
private val Logger = LoggerFactory.getLogger("EMI++")

@JvmOverloads
fun logInfo(message: String, component: Any? = null) {
    Logger.info(message, component)
}

@JvmOverloads
fun logWarn(message: String, component: Any? = null) {
    Logger.warn(message, component)
}

@JvmOverloads
fun logError(message: String, component: Any? = null) {
    Logger.error(message, component)
}

@JvmOverloads
fun logDebug(message: String, component: Any? = null) {
    if (isDebugMode) Logger.debug(message, component)
}

fun <T> logDebugTime(message: String, action: () -> T): T {
    val result: T
    val duration = measureTime { result = action() }
    if (isDebugMode) Logger.debug(message, duration)
    return result
}