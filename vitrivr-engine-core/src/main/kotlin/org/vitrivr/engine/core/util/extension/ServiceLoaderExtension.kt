package org.vitrivr.engine.core.util.extension

import java.util.*

/**
 * This utility function loads a service given a class name. The class name can either be simple or fully qualified.
 *
 * @param name The name of the class to load as service.
 * @return The loaded service.
 */
inline fun <reified T : Any> loadServiceForName(name: String): T? {
    val clazz = T::class.java
    val candidates = ServiceLoader.load(clazz)
    return if (name.contains('.')) {
        candidates.find { it::class.java.name == name }
    } else {
        val filtered = candidates.filter { it::class.java.simpleName == name }
        if (filtered.size == 1) {
            return filtered.first()
        } else if (filtered.size > 1) {
            throw IllegalArgumentException("The simple class name '$name' is not unique for type $clazz.")
        } else {
            return null
        }
    }
}