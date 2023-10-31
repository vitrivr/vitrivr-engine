package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*

/**
 * A [VectorDescriptor] that uses a [Long] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class LongVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: List<Long>,
    override val transient: Boolean = false
) : VectorDescriptor<Long> {
    /**
     * Returns the [FieldSchema] [List ]of this [LongVectorDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = listOf(FieldSchema("vector", FieldType.LONG, intArrayOf(this.dimensionality)))
}
