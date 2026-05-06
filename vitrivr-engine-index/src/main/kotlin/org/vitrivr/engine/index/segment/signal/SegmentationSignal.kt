package org.vitrivr.engine.index.segment.signal

import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * Temporary signal used only for segmentation decisions.
 * Implementations must not persist descriptors or modify the input retrievable.
 *
 * @author Rahel Arnold
 */
interface SegmentationSignal {
    val name: String
    val weight: Double

    suspend fun extract(retrievable: Retrievable): SignalValue?

    fun distance(previous: SignalValue, current: SignalValue): Double
}
