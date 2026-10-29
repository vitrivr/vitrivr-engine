package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.*
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator

/** Weighted reciprocal rank fusion over local segment IDs. Raw source scores are never added together. */
class ReciprocalRankFusion(
    private val sources: Map<String, Operator<out Retrievable>>,
    private val weights: Map<String, Double> = emptyMap(),
    private val rankConstant: Double = 60.0,
    private val limit: Int = 1000,
    private val requiredInputs: Set<String> = emptySet(),
    private val feedbackInput: String? = null,
    private val stateProperties: Set<String> = emptySet(),
    private val replay: RankFusionState? = null,
    private val restore: (List<RankFusionCandidate>) -> List<Retrievable> = { error("No replay loader configured.") },
    override val name: String
) : Aggregator {
    override val inputs = sources.values.toList()
    init {
        require(sources.isNotEmpty()) { "ReciprocalRankFusion requires an input." }
        require(weights.keys.all { it in sources } && requiredInputs.all { it in sources }) { "Unknown fusion input alias." }
        require(feedbackInput == null || feedbackInput in sources) { "Unknown feedback input alias." }
        require(weights.values.all { it.isFinite() && it >= 0 } && sources.keys.sumOf { weights[it] ?: 1.0 }.let { it.isFinite() && it > 0 }) { "Fusion weights must be finite, nonnegative, and not all zero." }
        require(rankConstant.isFinite() && rankConstant >= 0 && limit > 0)
        require(replay == null || replay.inputs.keys == sources.keys - setOfNotNull(feedbackInput)) {
            "Replay must contain exactly the local input aliases; the feedback input must be retrieved again."
        }
        require("fusion.state" !in stateProperties)
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        val ranked = coroutineScope {
            sources.map { (alias, input) -> async(Dispatchers.IO) {
                val values = replay?.inputs?.get(alias)?.let(restore) ?: input.toFlow(this).toList()
                values.forEach { result ->
                    val score = result.filteredAttribute(ScoreAttribute::class.java)?.score
                    require(score == null || score.isFinite() && score >= 0) { "Invalid score from '$alias'." }
                }
                // Stable score ordering preserves source order for ties and sources without explicit scores.
                alias to values.distinctBy { it.id }.sortedByDescending { it.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0 }
            } }.awaitAll().toMap()
        }
        val state = Json.encodeToString(RankFusionState(ranked.filterKeys { it != feedbackInput }
            .mapValues { (_, values) -> values.map(RankFusionCandidate::from) }))
        val carried = stateProperties.mapNotNull { key ->
            val values = ranked.values.flatten().flatMap { it.filteredAttributes(PropertyAttribute::class.java) }
                .mapNotNull { it.properties[key] }.distinct()
            require(values.size <= 1) { "Conflicting state property '$key'." }
            values.singleOrNull()?.let { key to it }
        }.toMap()
        val evidence = linkedMapOf<java.util.UUID, MutableList<Triple<String, Int, Retrievable>>>()
        ranked.forEach { (alias, values) -> values.forEachIndexed { index, result ->
            evidence.getOrPut(result.id) { mutableListOf() }.add(Triple(alias, index + 1, result))
        } }
        val results = evidence.entries.mapNotNull { (id, entries) ->
            val aliases = entries.map { it.first }.toSet()
            if (!aliases.containsAll(requiredInputs)) return@mapNotNull null
            val score = entries.sumOf { (alias, rank, _) -> (weights[alias] ?: 1.0) / (rankConstant + rank) }
            if (score == 0.0) return@mapNotNull null
            val first = entries.first().third
            val properties = entries.flatMap { it.third.filteredAttributes(PropertyAttribute::class.java) }
                .flatMap { it.properties.entries }.filterNot { it.key in stateProperties || it.key.startsWith("fusion.") }
                .associate { it.key to it.value } + mapOf(
                    "fusion.feedbackEligible" to (feedbackInput != null && feedbackInput in aliases).toString(),
                    "fusion.evidence" to buildJsonObject {
                        entries.forEach { (alias, rank, result) -> put(alias, buildJsonObject {
                            put("rank", rank)
                            put("score", result.filteredAttribute(ScoreAttribute::class.java)?.score?.let(::JsonPrimitive) ?: JsonNull)
                            put("weight", weights[alias] ?: 1.0)
                            put("contribution", (weights[alias] ?: 1.0) / (rankConstant + rank))
                        }) }
                    }.toString())
            val merged = first.copy(
                descriptors = entries.flatMap { it.third.descriptors }.toSet(),
                content = entries.flatMap { it.third.content }.distinct(),
                relationships = entries.flatMap { it.third.relationships }.toSet(),
                attributes = entries.flatMap { it.third.attributes }.filterNot { it is ScoreAttribute || it is PropertyAttribute }.toSet() +
                    ScoreAttribute.Unbound(score) + PropertyAttribute(properties))
            Triple(id, score, merged)
        }.sortedWith(compareByDescending<Triple<java.util.UUID, Double, Retrievable>> { it.second }.thenBy { it.first.toString() }).take(limit)
        results.forEachIndexed { index, (_, _, result) ->
            if (index == 0) {
                val properties = result.filteredAttribute(PropertyAttribute::class.java)!!.properties + carried + ("fusion.state" to state)
                emit(result.copy(attributes = result.attributes.filterNot { it is PropertyAttribute }.toSet() + PropertyAttribute(properties)))
            } else emit(result)
        }
    }
}
