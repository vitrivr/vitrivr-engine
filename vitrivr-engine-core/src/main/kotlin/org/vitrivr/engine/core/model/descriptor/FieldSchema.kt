package org.vitrivr.engine.core.model.descriptor

import org.vitrivr.engine.core.model.types.Type

/**
 * A [FieldSchema] describes a field in a [Descriptor]. This is required for a [Descriptor]'s ability to
 * describe its own schema.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class FieldSchema(val name: String, val type: Type, val dimensions: IntArray = intArrayOf(), val nullable: Boolean = false)