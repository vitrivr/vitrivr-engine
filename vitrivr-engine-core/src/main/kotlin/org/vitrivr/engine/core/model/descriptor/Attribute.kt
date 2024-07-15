package org.vitrivr.engine.core.model.descriptor

import org.vitrivr.engine.core.model.types.Type

/** The name of an attribute. */
typealias AttributeName = String

/**
 * A [Attribute] describes a field in a [Descriptor]. This is required for a [Descriptor]'s ability to
 * describe its own schema.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class Attribute(val name: AttributeName, val type: Type, val nullable: Boolean = false)