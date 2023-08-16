package org.vitrivr.engine.core.data.descriptor

import org.vitrivr.engine.core.describe.DescriberId
import java.util.*

data class FloatVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val transient: Boolean = false,
    override val describerId: DescriberId,
    val vector: FloatArray
) : VectorDescriptor {

    override val dimensionality: Int
        get() = this.vector.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FloatVectorDescriptor

        if (id != other.id) return false
        if (transient != other.transient) return false
        if (describerId != other.describerId) return false
        if (!vector.contentEquals(other.vector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + transient.hashCode()
        result = 31 * result + describerId.hashCode()
        result = 31 * result + vector.contentHashCode()
        return result
    }
}
