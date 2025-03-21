package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import kotlin.math.max

/**
 * A [RetrievableAttribute] that represents a similarity or relevance score, i.e., the higher the score value the more relevant or similar it is.
 *
 * Scores are expected to be in the range of [0, 1].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
sealed interface ScoreAttribute : RetrievableAttribute {

    /** The score associated with this [ScoreAttribute]. */
    val score: Double

    /**
     * A global similarity score. Strictly bound between 0 and 1.
     */
    data class Similarity(override val score: Double, val descriptorId: DescriptorId? = null): ScoreAttribute, MergingRetrievableAttribute {
        init {
            require(score in 0.0..1.0) { "Similarity score '$score' outside of valid range (0, 1)" }
        }

        override fun merge(other: MergingRetrievableAttribute): Similarity = Similarity(
            max(this.score, (other as? Similarity)?.score ?: 0.0)
        )
    }

    /**
     * A global unbound score. Unbounded and can be any value >= 0.
     */
    data class Unbound(override val score: Double, val descriptorId: DescriptorId? = null): ScoreAttribute, MergingRetrievableAttribute {
        init {
            require(this.score >= 0f) { "Score '$score' outside of valid range (>= 0)." }
        }
        override fun merge(other: MergingRetrievableAttribute): Unbound = Unbound(
            max(this.score, (other as? Unbound)?.score ?: 0.0)
        )
    }
}

