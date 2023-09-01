package org.vitrivr.engine.core.config

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
@JvmRecord
@Serializable
data class FieldConfig(val name: String, val analyser: String, val parameters: Map<String,String> = emptyMap())