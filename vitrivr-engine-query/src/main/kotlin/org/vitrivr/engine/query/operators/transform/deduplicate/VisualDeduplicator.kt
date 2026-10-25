package org.vitrivr.engine.query.operators.transform.deduplicate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Greedy, score-ordered suppression of similar nearby results from the same video.
 * Only retained results suppress other candidates; a chain of discarded results
 * cannot collapse an entire recording. Missing or invalid metadata passes through.
 * The final limit applies after suppression. Stored descriptors are never changed.
 */
class VisualDeduplicator(
    override val input: Operator<out Retrievable>,
    private val loadSignatures: (Set<RetrievableId>) -> Map<RetrievableId, Signature>,
    private val similarityThreshold: Double = 0.98,
    private val timeWindowSeconds: Double = 60.0,
    private val limit: Int = 20,
    override val name: String
) : Transformer {
    /** Start time follows TemporalMetadataDescriptor: nanoseconds relative to the video. */
    data class Signature(val videoPath: String, val startNs: Long, val vector: FloatArray)

    private data class Normalized(val videoPath: String, val startNs: Long, val vector: DoubleArray)

    init {
        require(similarityThreshold.isFinite() && similarityThreshold in -1.0..1.0) {
            "similarityThreshold must be a finite cosine similarity between -1 and 1."
        }
        require(timeWindowSeconds.isFinite() && timeWindowSeconds >= 0.0 &&
            timeWindowSeconds <= Long.MAX_VALUE.toDouble() / 1_000_000_000.0) {
            "timeWindowSeconds must be finite, non-negative and fit in nanoseconds."
        }
        require(limit > 0) { "limit must be positive." }
    }

    private val timeWindowNs = (timeWindowSeconds * 1_000_000_000.0).toLong()

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        val candidates = input.toFlow(scope).toList().sortedByDescending {
            it.filteredAttribute(ScoreAttribute::class.java)?.score?.takeIf { score -> score.isFinite() }
                ?: Double.NEGATIVE_INFINITY
        }.distinctBy { it.id }
        if (candidates.isEmpty()) return@flow
        val signatures = loadSignatures(candidates.map { it.id }.toSet())
        val keptByVideo = mutableMapOf<String, MutableList<Normalized>>()
        var emitted = 0
        for (candidate in candidates) {
            val signature = signatures[candidate.id]?.normalize()
            if (signature != null) {
                val kept = keptByVideo.getOrPut(signature.videoPath) { mutableListOf() }
                val duplicate = kept.any { previous ->
                    abs(previous.startNs - signature.startNs) <= timeWindowNs &&
                        previous.vector.size == signature.vector.size &&
                        cosine(previous.vector, signature.vector) >= similarityThreshold
                }
                if (duplicate) continue
                kept.add(signature)
            }
            emit(candidate)
            if (++emitted >= limit) break
        }
    }

    private fun Signature.normalize(): Normalized? {
        if (videoPath.isBlank() || startNs < 0 || vector.isEmpty() || vector.any { !it.isFinite() }) return null
        val norm = sqrt(vector.sumOf { it.toDouble() * it.toDouble() })
        if (norm == 0.0 || !norm.isFinite()) return null
        return Normalized(videoPath, startNs, DoubleArray(vector.size) { vector[it] / norm })
    }

    private fun cosine(left: DoubleArray, right: DoubleArray): Double =
        left.indices.sumOf { left[it] * right[it] }.coerceIn(-1.0, 1.0)
}
