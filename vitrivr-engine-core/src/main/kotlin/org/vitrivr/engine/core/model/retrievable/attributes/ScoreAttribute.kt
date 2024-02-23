package org.vitrivr.engine.core.model.retrievable.attributes

import kotlin.math.max

data class ScoreAttribute(val score: Float) : MergingRetrievableAttribute {

    init {
        require(score in 0f..1f) { "Score '$score' outside of valid range (0, 1)" }
    }

    override fun merge(other: MergingRetrievableAttribute): ScoreAttribute =
        ScoreAttribute(max(this.score, (other as? ScoreAttribute)?.score ?: 0f))

}
