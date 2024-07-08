package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [VectorDescriptor] that uses a [Double] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */

data class DoubleVectorDescriptor(
    override var id: UUID = UUID.randomUUID(),
    override var retrievableId: RetrievableId? = null,
    override val vector: Value.DoubleVector,
    override val field: Schema.Field<*, DoubleVectorDescriptor>? = null
) : VectorDescriptor<Value.DoubleVector> {
    /**
     * Returns the [Attribute] [List ]of this [DoubleVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun schema(): List<Attribute> = listOf(Attribute("vector", Type.DoubleVector(this.dimensionality)))
}
