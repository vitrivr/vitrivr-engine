package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
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
 * @version 1.1.0
 */

data class BooleanVectorDescriptor(
    override var id: UUID = UUID.randomUUID(),
    override var retrievableId: RetrievableId? = null,
    override val vector: Value.BooleanVector,
    override val field: Schema.Field<*, BooleanVectorDescriptor>? = null
) : VectorDescriptor<Value.BooleanVector> {
    /**
     * Returns the [Attribute] [List ]of this [BooleanVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.BooleanVector(this.dimensionality)))
}
