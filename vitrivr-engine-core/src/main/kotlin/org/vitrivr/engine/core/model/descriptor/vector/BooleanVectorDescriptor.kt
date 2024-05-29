package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [VectorDescriptor] that uses a [Boolean] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class BooleanVectorDescriptor(
    override var id: UUID = UUID.randomUUID(),
    override var retrievableId: RetrievableId? = null,
    override val vector: List<Value.Boolean>,
    override val field: Schema.Field<*, BooleanVectorDescriptor>? = null
) : VectorDescriptor<Value.Boolean> {
    /**
     * Returns the [FieldSchema] [List ]of this [BooleanVectorDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = listOf(FieldSchema("vector", Type.BOOLEAN, intArrayOf(this.dimensionality)))
}
