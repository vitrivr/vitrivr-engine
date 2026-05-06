package org.vitrivr.engine.index.segment.signal

/**
 * Generic vector-valued segmentation signal.
 *
 * @author Rahel Arnold
 */
data class SignalValue(
    val vector: DoubleArray
) {
    override fun equals(other: Any?): Boolean = other is SignalValue && vector.contentEquals(other.vector)
    override fun hashCode(): Int = vector.contentHashCode()
}
