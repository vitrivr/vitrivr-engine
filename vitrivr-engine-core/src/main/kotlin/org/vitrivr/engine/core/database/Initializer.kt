package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.Persistable

/**
 * The [Initializer] is part of the data persistence layer of vitrivr and can be used to encapsulate DDL operations
 * for the underlying database  (e.g., creation of entities, index structures etc.).
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface Initializer<out T : Persistable> {
    /**
     * Initializes the (persistent) data structures required by the [Persistable] of type [T].
     */
    fun initialize()

    /**
     * Returns true if the structures created by this [Initializer] have already been created and setup.
     *
     * @return True, if initialization is required, false otherwise.
     */
    fun isInitialized(): Boolean

    /**
     * Truncates the (persistent) data structures required by the [Persistable] of type [T].
     */
    fun truncate()
}