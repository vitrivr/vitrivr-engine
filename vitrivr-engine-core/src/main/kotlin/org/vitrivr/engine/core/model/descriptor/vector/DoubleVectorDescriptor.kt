package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*

/**
 * A [VectorDescriptor] that uses a [Double] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class DoubleVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: List<Double>,
    override val transient: Boolean = false
) : VectorDescriptor<Double>
