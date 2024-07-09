package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [VectorDescriptor] that uses a [Long] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */

data class LongVectorDescriptor(
    override var id: UUID = UUID.randomUUID(),
    override var retrievableId: RetrievableId? = null,
    override val vector: Value.LongVector,
    override val field: Schema.Field<*, LongVectorDescriptor>? = null
) : VectorDescriptor<Value.LongVector> {
    /**
     * Returns the [Attribute] [List ]of this [LongVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun schema(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.Long))
}
