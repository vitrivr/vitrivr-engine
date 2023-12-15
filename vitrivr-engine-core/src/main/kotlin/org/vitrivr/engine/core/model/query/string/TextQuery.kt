package org.vitrivr.engine.core.model.query.string

import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.query.Query


/**
 * A [TextQuery] that uses a [StringDescriptor].
 *
 * A [TextQuery] is typically translated to a fulltext query in the underlying storage engine.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TextQuery(override val descriptor: StringDescriptor, val limit: Long = Long.MAX_VALUE) : Query<StringDescriptor>
