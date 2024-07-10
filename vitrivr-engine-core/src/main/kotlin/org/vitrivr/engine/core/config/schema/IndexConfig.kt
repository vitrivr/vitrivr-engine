package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A serializable configuration object to configure indexes for a [Schema.Field] within a [Schema].
 *
 * @see [Schema.Field]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class IndexConfig(val attributes: List<AttributeName>, val type: IndexType, val parameters: Map<String,String> = emptyMap())