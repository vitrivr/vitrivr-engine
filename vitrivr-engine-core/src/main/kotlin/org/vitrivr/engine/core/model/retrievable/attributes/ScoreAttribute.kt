package org.vitrivr.engine.core.model.retrievable.attributes

import kotlin.math.max

/**
 * A [RetrievableAttribute] that represents a similarity or relevance score, i.e., the higher the score value the more relevant or similar it is.
 *
 * Scores are expected to be in the range of [0, 1].
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
data class ScoreAttribute(val score: Float) : MergingRetrievableAttribute {

    companion object {
        val ZERO = ScoreAttribute(0f)
    }

    constructor(score: Double) : this(score.toFloat())

    init {
        require(score in 0f..1f) { "Score '$score' outside of valid range (0, 1)" }
    }

    override fun merge(other: MergingRetrievableAttribute): ScoreAttribute = ScoreAttribute(
        max(this.score, (other as? ScoreAttribute)?.score ?: 0f)
    )
}
