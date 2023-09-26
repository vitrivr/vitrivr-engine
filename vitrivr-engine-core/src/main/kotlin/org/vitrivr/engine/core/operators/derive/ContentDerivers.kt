package org.vitrivr.engine.core.operators.derive

import java.util.ServiceLoader

object ContentDerivers {

    private val registry = mutableMapOf<DerivateName, ContentDeriver<*>>()

    init {
        val serviceLoader = ServiceLoader.load(ContentDeriver::class.java)
        serviceLoader.forEach {
            registry[it.derivateName] = it
        }
    }

    operator fun get(derivateName: DerivateName): ContentDeriver<*>? = registry[derivateName]

}