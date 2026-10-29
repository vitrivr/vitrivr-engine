package org.vitrivr.engine.query.aggregate

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/** Local ranking evidence retained by the client, scoped to one federated search. */
@Serializable
data class RankFusionState(val inputs: Map<String, List<RankFusionCandidate>>) {
    init {
        inputs.values.flatten().forEach { candidate ->
            java.util.UUID.fromString(candidate.id)
            require(candidate.score == null || candidate.score.isFinite() && candidate.score >= 0)
        }
    }
}

@Serializable
data class RankFusionCandidate(
    val id: String,
    val score: Double? = null,
    val properties: Map<String, String> = emptyMap(),
    /** Field names and descriptor IDs allow replay to reload the same evidence without a new search. */
    val descriptors: Map<String, List<String>> = emptyMap()
) {
    companion object {
        fun from(result: Retrievable) = RankFusionCandidate(result.id.toString(),
            result.filteredAttribute(ScoreAttribute::class.java)?.score,
            result.filteredAttributes(PropertyAttribute::class.java).flatMap { it.properties.entries }
                .filterNot { it.key in setOf("fusion.state", "prak.candidates", "prak.localState", "prak.localFeedback") }.associate { it.key to it.value },
            result.descriptors.filter { it.field != null }.groupBy { it.field!!.fieldName }
                .mapValues { (_, values) -> values.map { it.id.toString() } })
    }
}
