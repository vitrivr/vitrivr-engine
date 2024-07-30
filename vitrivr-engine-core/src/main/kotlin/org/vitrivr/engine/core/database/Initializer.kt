package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.query.basics.Distance

/**
 * The [Initializer] is part of the data persistence layer of vitrivr and can be used to encapsulate DDL operations
 * for the underlying database  (e.g., creation of entities, index structures etc.).
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
interface Initializer<out T : Persistable> {

    companion object {
        /** The column name used to specify type of index. This is database specific.*/
        const val INDEX_TYPE_PARAMETER_NAME = "type"

        /** The parameter name used to specify the distance. Used upon index creation. References [Distance].*/
        const val DISTANCE_PARAMETER_NAME = "distance"
    }

    /**
     * Initializes the (persistent) data structures required by the [Persistable] of type [T].
     */
    fun initialize()

    /**
     * De-initializes the (persistent) data structures required by the [Persistable] of type [T].
     */
    fun deinitialize()

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