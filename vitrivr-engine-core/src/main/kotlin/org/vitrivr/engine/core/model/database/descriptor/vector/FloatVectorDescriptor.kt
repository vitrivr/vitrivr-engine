package org.vitrivr.engine.core.model.database.descriptor.vector

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
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
    override val retrievableId: RetrievableId? = null,
    override val vector: List<Float>,
    override val transient: Boolean = false
) : VectorDescriptor<Float>
