package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*

/**
 * A [VectorDescriptor] that uses a [Boolean] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class BooleanVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: List<Boolean>,
    override val transient: Boolean = false
) : VectorDescriptor<Boolean> {
    /**
     * Returns the [FieldSchema] [List ]of this [BooleanVectorDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = listOf(FieldSchema("vector", FieldType.BOOLEAN, intArrayOf(this.dimensionality)))
}
