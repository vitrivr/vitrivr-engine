package org.vitrivr.engine.core.context

import io.javalin.openapi.OpenApiIgnore
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [Context] provides additional contextual configuration in the form of key-value pairs,
 * both on a [global] and [local] level. For [local] key-value pairs, the name of the operator is required.
 * This is applicable for index and query phases.
 */
@Serializable
sealed class Context {
    /**
     * Configuration per named operator.
     */
    abstract val local: Map<String, Map<String, String>>

    /** properties applicable to all operators */
    abstract val global: Map<String, String>

    /**
     * Provides the [property] for the [operator], in case it is defined.
     * Defining a property [local]ly overrides a [global] one.
     *
     * @param operator The name of the operator to get the property for.
     * @param property The name of the property to get.
     *
     * @return Either the value named [property] for the [operator] or NULL, in case no such property exists.
     */
    @OpenApiIgnore
    operator fun get(operator: String, property: String): String? = getProperty(operator, property)

    /**
     * Provides the [property] for the [operator], in case it is defined.
     * Defining a property [local]ly overrides a [global] one.
     *
     * @param operator The name of the operator to get the property for.
     * @param property The name of the property to get.
     *
     * @return Either the value named [property] for the [operator] or NULL, in case no such property exists.
     */
    @OpenApiIgnore
    fun getProperty(operator: String, property: String): String? =
        local[operator]?.get(property) ?: global[property]

    /**
     * Provides the [property] for the [operator], or the [default] otherwise.
     * Defining a property [local]ly overrides a [global] one.
     *
     * @param operator The name of the operator to get the property for.
     * @param property The name of the property to get.
     * @param default The default value to return, in case none would have been found.
     *
     * @return Either the value named [property] for the [operator] or [default]
     */
    fun getPropertyOrDefault(operator: String, property: String, default: String): String = getProperty(operator, property) ?: default

    @get:OpenApiIgnore
    abstract val schema: Schema
}
