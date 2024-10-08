package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A serializable configuration object to configure [Schema.Field] within a [Schema].
 *
 * @see [Schema.Field]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class FieldConfig(val factory: String, val parameters: Map<String,String> = emptyMap(), val indexes: List<IndexConfig> = emptyList())
