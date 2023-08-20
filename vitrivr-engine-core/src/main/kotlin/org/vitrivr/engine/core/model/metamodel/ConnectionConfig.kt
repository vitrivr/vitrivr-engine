package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


/**
 * A [ConnectionConfig], which is part of a [Schema].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class ConnectionConfig(val typeName: String, val parameters: Map<String,String> = emptyMap()) {

    /**
     * Returns the [KClass] for this [ConnectionConfig]'s [typeName].
     *
     * @return [KClass] of the [Connection].
     * @throws [ClassNotFoundException] if class could not be found.
     */
    @Suppress("UNCHECKED_CAST")
    fun connectionClass(): KClass<Connection> {
        val clazz = Class.forName(typeName).kotlin
        require(clazz.isSubclassOf(Connection::class)) {
            "The provided class of type $typeName is not a valid database connection class."
        }
        return clazz as KClass<Connection>
    }
}