package org.vitrivr.engine.query.aggregate

import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import java.util.UUID

class ReciprocalRankFusionFactory : OperatorFactory {
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): ReciprocalRankFusion {
        val parameters = context.global + context.local[name].orEmpty()
        fun names(key: String) = parameters[key]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet().orEmpty()
        fun number(key: String, default: Double) = parameters[key]?.let {
            it.toDoubleOrNull() ?: throw IllegalArgumentException("$key must be numeric.")
        } ?: default
        val weights = parameters.filterKeys { it.startsWith("weight.") }.map { (key, value) ->
            key.removePrefix("weight.") to (value.toDoubleOrNull() ?: throw IllegalArgumentException("$key must be numeric."))
        }.toMap()
        val limit = parameters["limit"]?.let { it.toIntOrNull() ?: throw IllegalArgumentException("limit must be an integer.") } ?: 1000
        return ReciprocalRankFusion(inputs, weights, number("rankConstant", 60.0), limit,
            names("requiredInputs"), parameters["feedbackInput"], names("stateProperties"),
            parameters["replay"]?.let { Json.decodeFromString<RankFusionState>(it) },
            restore = { candidates -> restore(candidates, context) }, name = name)
    }

    /** Reload original evidence by ID; do not execute the original OCR/ASR search again. */
    private fun restore(candidates: List<RankFusionCandidate>, context: Context): List<Retrievable> {
        if (candidates.isEmpty()) return emptyList()
        require(candidates.map { it.id }.distinct().size == candidates.size) { "Duplicate replay candidate IDs." }
        val ids = candidates.map { UUID.fromString(it.id) }
        val stored = ids.chunked(10_000).flatMap { context.schema.connection.getRetrievableReader().getAll(it).toList() }.associateBy { it.id }
        require(stored.keys.containsAll(ids)) { "Cached fusion candidates no longer exist; start a new search." }
        val references = candidates.flatMap { candidate -> candidate.descriptors.map { (field, ids) -> field to ids } }
            .groupBy({ it.first }, { it.second }).mapValues { (_, ids) -> ids.flatten().map(UUID::fromString).distinct() }
        val descriptors = references.mapValues { (fieldName, ids) ->
            val field = requireNotNull(context.schema[fieldName]) { "Unknown replay descriptor field '$fieldName'." }
            ids.chunked(10_000).flatMap { field.getReader().getAll(it).toList() }.associateBy { it.id }
        }
        return candidates.map { candidate ->
            val id = UUID.fromString(candidate.id)
            val item = stored.getValue(id)
            val evidence = candidate.descriptors.flatMap { (field, ids) -> ids.map { value ->
                requireNotNull(descriptors[field]?.get(UUID.fromString(value))) { "Replay evidence no longer exists; start a new search." }.also {
                    require(it.retrievableId == id) { "Replay descriptor belongs to a different segment." }
                }
            } }.toSet()
            item.copy(descriptors = evidence, attributes = item.attributes.filterNot { it is ScoreAttribute || it is PropertyAttribute }.toSet() +
                PropertyAttribute(candidate.properties) + listOfNotNull(candidate.score?.let { ScoreAttribute.Unbound(it) }))
        }
    }
}
