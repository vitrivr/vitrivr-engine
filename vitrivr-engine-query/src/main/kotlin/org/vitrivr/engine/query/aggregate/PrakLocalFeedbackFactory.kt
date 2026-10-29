package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

@Serializable
data class PrakLocalFeedbackState(val candidates: List<RankFusionCandidate>, val remote: JsonObject? = null) {
    init {
        require(candidates.map { it.id }.distinct().size == candidates.size) { "Duplicate local candidate IDs." }
        RankFusionState(mapOf("local" to candidates))
    }
}

/** Initial pass-through saves a local pool; feedback-only requests invoke the configured PRAK analyser. */
class PrakLocalFeedbackFactory : OperatorFactory {
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
        val state = context[name, "state"]?.let { Json.decodeFromString<PrakLocalFeedbackState>(it) }
        val selected = context[name, "selectedRetrievableIds"]?.let { Json.decodeFromString<List<String>>(it) }.orEmpty().distinct()
        val limit = context[name, "limit"]?.let { it.toIntOrNull() ?: throw IllegalArgumentException("limit must be a positive integer.") } ?: 1000
        require(limit > 0)
        require(if (state == null) inputs.size == 1 && selected.isEmpty() else inputs.isEmpty() && selected.isNotEmpty()) {
            "Initial PrakLocalFeedback needs one local input; feedback needs state and selectedRetrievableIds with no input operations."
        }
        val fieldName = context[name, "prakField"] ?: "prak"
        val field = requireNotNull(context.schema[fieldName]) { "Configure a PRAK field '$fieldName' for remote feedback." }
        require(field.analyser::class.simpleName == "PRAK") { "prakField must use the PRAK analyser." }
        val localInputs = inputs.values.toList()
        return object : Operator.NAry<Retrievable, Retrievable> {
            override val name = name
            override val inputs = localInputs
            override fun root(): Operator<*> = localInputs.firstOrNull()?.root() ?: this
            override fun toFlow(scope: CoroutineScope) = flow {
                if (state == null) {
                    val results = localInputs.single().toFlow(scope).toList().distinctBy { it.id }
                        .sortedByDescending { it.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0 }.take(limit)
                    val snapshot = PrakLocalFeedbackState(results.map(RankFusionCandidate::from))
                    results.forEachIndexed { index, result ->
                        val original = result.filteredAttributes(PropertyAttribute::class.java).flatMap { it.properties.entries }.associate { it.key to it.value }
                        val props = original + mapOf("fusion.feedbackEligible" to "true", "fusion.feedbackMode" to "nearest", "prak.feedbackSource" to "local-pool") +
                            if (index == 0) mapOf("prak.localState" to Json.encodeToString(snapshot)) else emptyMap()
                        emit(result.copy(attributes = result.attributes.filterNot { it is PropertyAttribute }.toSet() + PropertyAttribute(props)))
                    }
                } else {
                    val ids = state.candidates.map { it.id }
                    require(selected.all { it in ids }) { "Select results from the saved local pool." }
                    val candidates = ReciprocalRankFusionFactory().restore(state.candidates, context).associateBy { it.id.toString() }
                    val remote = state.remote.orEmpty().toMutableMap()
                    if (state.remote != null) require(remote["localCandidates"]?.jsonArray?.map { it.jsonPrimitive.content } == ids) { "Remote mapping belongs to a different local pool." }
                    remote["localCandidates"] = JsonArray(ids.map(::JsonPrimitive))
                    remote["selectedRetrievableIds"] = JsonArray(selected.map(::JsonPrimitive))
                    context[name, "max_rank"]?.let {
                        val rank = it.toIntOrNull() ?: throw IllegalArgumentException("max_rank must be an integer.")
                        remote["max_rank"] = JsonPrimitive(rank)
                    }
                    val parameters = mapOf("feedback" to JsonObject(remote).toString(), "limit" to ids.size.coerceAtLeast(1).toString()) +
                        listOfNotNull(context[name, "feedbackMaxDistanceMs"]?.let { "feedbackMaxDistanceMs" to it }).toMap()
                    val retrievalContext = context.copy(local = context.local + (fieldName to parameters))
                    val retriever = field.getRetrieverForContent(mapOf("text" to context.contentFactory.newTextContent("")), retrievalContext)
                    val ranked = retriever.toFlow(scope).toList()
                    require(ranked.size == ids.size && ranked.map { it.id.toString() }.toSet() == ids.toSet()) { "Remote feedback changed the local result pool." }
                    val remoteState = ranked.flatMap { it.filteredAttributes(PropertyAttribute::class.java) }.mapNotNull { it.properties["prak.localFeedback"] }.single()
                    val updated = state.copy(remote = Json.parseToJsonElement(remoteState).jsonObject)
                    ranked.forEachIndexed { index, result ->
                        val original = candidates.getValue(result.id.toString())
                        val oldProps = original.filteredAttributes(PropertyAttribute::class.java).flatMap { it.properties.entries }.associate { it.key to it.value }
                        val newProps = result.filteredAttributes(PropertyAttribute::class.java).flatMap { it.properties.entries }
                            .filterNot { it.key == "prak.localFeedback" }.associate { it.key to it.value }
                        val props = oldProps + newProps + mapOf("prak.feedbackSource" to "local-pool") +
                            if (index == 0) mapOf("prak.localState" to Json.encodeToString(updated)) else emptyMap()
                        emit(original.copy(attributes = original.attributes.filterNot { it is ScoreAttribute || it is PropertyAttribute }.toSet() +
                            result.attributes.filterIsInstance<ScoreAttribute>() + PropertyAttribute(props)))
                    }
                }
            }
        }
    }
}
