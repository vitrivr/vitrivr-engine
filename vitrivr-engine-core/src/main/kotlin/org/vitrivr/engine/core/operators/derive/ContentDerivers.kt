package org.vitrivr.engine.core.operators.derive

object ContentDerivers {

    private val registry = mutableMapOf<DerivateName, ContentDeriver<*>>()

    fun register(deriver: ContentDeriver<*>) {
        registry[deriver.derivateName] = deriver
    }

    operator fun get(derivateName: DerivateName): ContentDeriver<*>? = registry[derivateName]

}