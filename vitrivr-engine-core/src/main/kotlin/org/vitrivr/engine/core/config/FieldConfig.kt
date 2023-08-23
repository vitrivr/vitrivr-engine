package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A serializable configuration object to configure [Field] within a [Schema].
 *
 * @see [Field]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class FieldConfig(val fieldName: String, val analyserName: String, val parameters: Map<String,String> = emptyMap())