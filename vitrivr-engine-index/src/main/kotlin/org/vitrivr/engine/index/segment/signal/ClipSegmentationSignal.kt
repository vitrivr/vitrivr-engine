package org.vitrivr.engine.index.segment.signal

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.index.segment.util.cosineDistance

/** Segmentation signal based on CLIP embeddings.
*
* Computes embedding distances between retrievables to detect semantic changes in video streams.
*
* @author Rahel Arnold
*/
class ClipSegmentationSignal(
    override val weight: Double,
    private val provider: EmbeddingProvider
) : SegmentationSignal {

    override val name: String = "clip"

    override suspend fun extract(retrievable: Retrievable): SignalValue? {
        return SignalValue(provider.embed(retrievable))
    }

    override fun distance(previous: SignalValue, current: SignalValue): Double {
        return cosineDistance(previous.vector, current.vector)
    }

    companion object {
        fun fromContext(operatorName: String, context: Context): ClipSegmentationSignal {
            val weight = (context[operatorName, "clipWeight"] ?: "1.0").toDouble()

            return ClipSegmentationSignal(
                weight = weight,
                provider = VitrivrClipEmbeddingAdapter(context)
            )
        }
    }
}