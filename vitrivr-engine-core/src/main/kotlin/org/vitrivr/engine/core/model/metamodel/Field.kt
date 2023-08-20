package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.model.database.descriptor.Descriptor

/**
 * A individual [Field] in the vitrivr meta model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class Field<T: Descriptor>(val fieldName: String, val analyserName: String, val parameters: Map<String,String> = emptyMap())