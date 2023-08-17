package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.database.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.query.Query

/**
 * A [BooleanQuery] that uses a [ScalarDescriptor] of type [T].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class BooleanQuery<T>(override val descriptor: ScalarDescriptor<T>): Query<ScalarDescriptor<T>>