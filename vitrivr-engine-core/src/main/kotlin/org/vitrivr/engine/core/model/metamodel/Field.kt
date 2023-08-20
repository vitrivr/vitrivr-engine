package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.operators.Describer
import org.vitrivr.engine.core.operators.DescriberId
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * A individual [Field] in the vitrivr meta model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class Field(val typeName: String, val name: String, val parameters: Map<String,String> = emptyMap()) {
    /**
     * Returns the [KClass] for this [ConnectionConfig]'s [typeName].
     *
     * @return [KClass] of the [Connection].
     * @throws [ClassNotFoundException] if class could not be found.
     */
    @Suppress("UNCHECKED_CAST")
    fun describerClass(): KClass<Describer<*>> {
        val clazz = Class.forName(typeName).kotlin
        require(clazz.isSubclassOf(Describer::class)) {
            "The provided class of type $typeName is not a valid describer class."
        }
        return clazz as KClass<Describer<*>>
    }

    /**
     * Creates and returns a new [Describer] instance using reflection.
     *
     * @return [Describer]
     */
    fun newDescriber(): Describer<*> {
        val type = this.describerClass()
        val constructor = type.constructors.find { constructor ->
            constructor.parameters.size == 1 && constructor.parameters.any { it.name == "parameters" }
        } ?: throw IllegalArgumentException("Failed to find valid constructor for describer of type $type.")
        return constructor.call(this.parameters)
    }
}