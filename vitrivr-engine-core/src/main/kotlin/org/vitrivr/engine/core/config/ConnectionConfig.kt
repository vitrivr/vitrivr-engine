package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.database.Connection

/**
 * A serializable configuration object to configure a database [Connection].
 *
 * @see [Connection]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
@Serializable
data class ConnectionConfig(val databaseName: String, val parameters: Map<String,String> = emptyMap())