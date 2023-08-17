package org.vitrivr.engine.core.model.database.descriptor.vector

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.DescriberId
import java.util.*


/**
 * A [VectorDescriptor] that uses a [FloatArray].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class FloatVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId,
    override val transient: Boolean = false,
    override val describerId: DescriberId,
    override val vector: List<Float>
) : VectorDescriptor<Float>
