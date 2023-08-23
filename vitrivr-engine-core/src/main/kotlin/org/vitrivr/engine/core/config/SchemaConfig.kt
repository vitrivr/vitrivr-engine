package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A serializable configuration object to configure [Schema].
 *
 * @see [Schema]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class SchemaConfig(val name: String = "vitrivr", val connection: ConnectionConfig, val fields: List<FieldConfig>)