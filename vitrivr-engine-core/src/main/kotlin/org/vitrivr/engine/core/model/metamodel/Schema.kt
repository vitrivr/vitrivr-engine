package org.vitrivr.engine.core.model.metamodel


/**
 * A [Schema] that defines a particular vitrivr instance's meta data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class Schema(
    val name: String = "vitrivr",
    val connection: ConnectionConfig,
    val fields: List<Field>
)