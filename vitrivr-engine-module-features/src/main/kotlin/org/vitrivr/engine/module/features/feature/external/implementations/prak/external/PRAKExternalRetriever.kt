package org.vitrivr.engine.module.features.feature.external.implementations.prak

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID

class PRAKExternalRetriever<C : ContentElement<*>>(
    field: Schema.Field<C, TextDescriptor>,
    private val queryDescriptor: TextDescriptor,
    context: Context,
    private val hostname: String,
    private val k: Int
) : AbstractRetriever<C, TextDescriptor>(field, SimpleBooleanQuery<Value.Text>(
    value = queryDescriptor.value, attributeName = field.fieldName, limit = k.toLong()), context) {

    private fun parameter(name: String, default: String) = context[field.fieldName, name] ?: field.parameters[name] ?: default

    override fun toFlow(scope: CoroutineScope) = flow {
        require(k > 0) { "PraK limit must be positive." }
        val mapper = PrakSegmentMapper(field.schema, parameter("fileField", "file"), parameter("timeField", "time"), parameter("recordingField", "recording"))
        val dataset = parameter("dataset", "CASTLE")
        val model = parameter("model", "siglip2")
        val feedback = context[field.fieldName, "feedback"]?.let { PrakProtocol.json.decodeFromString<PrakFeedback>(it) }
        val body: String
        val endpoint: String
        if (feedback == null) {
            endpoint = "textQuery"
            body = PrakProtocol.json.encodeToString(PrakQueryRequest(k = k, dataset = dataset, model = model, queries = queryDescriptor.value.value))
        } else {
            endpoint = "bayes"
            val localIds = feedback.selectedRetrievableIds.map { UUID.fromString(it) }.toSet()
            val candidateMatches = if (localIds.isEmpty()) emptyList() else mapper.mapResults(feedback.candidates)
            val mapped = candidateMatches.filter { (_, ids) -> ids.any { it in localIds } }.map { it.first.id }
            val mappedIds = candidateMatches.flatMap { it.second }.toSet()
            require(localIds.all { it in mappedIds }) {
                "Selected segments must map to a frame in the previous PraK candidates."
            }
            body = PrakProtocol.json.encodeToString(PrakProtocol.bayes(feedback, feedback.selectedImages + mapped, dataset, model))
        }
        val client = HttpClient(CIO) { expectSuccess = true; followRedirects = false }
        try {
            val requestStart = System.nanoTime()
            val response = client.post("${hostname.trimEnd('/')}/$endpoint/") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(body)
            }
            check(response.status.isSuccess()) { "PraK returned ${response.status}." }
            val results = PrakProtocol.results(response.bodyAsText())
            logger.info { "PraK $endpoint returned ${results.size} frames in ${(System.nanoTime() - requestStart) / 1_000_000} ms." }
            if (feedback != null) {
                require(results.map { it.id }.toSet() == feedback.candidates.map { it.id }.toSet() && results.size == feedback.candidates.size) {
                    "PraK Bayes returned an empty or incomplete candidate list; retain the previous ranking."
                }
            }
            val mappingStart = System.nanoTime()
            val matches = mapper.mapResults(results)
            val framesBySegment = mutableMapOf<UUID, MutableList<PrakApiResult>>()
            matches.forEach { (result, ids) -> ids.forEach { id -> framesBySegment.getOrPut(id) { mutableListOf() }.add(result) } }
            logger.info { "PraK mapped ${results.size} frames to ${framesBySegment.size} segments in ${(System.nanoTime() - mappingStart) / 1_000_000} ms." }
            val ids = framesBySegment.keys
            if (ids.isNotEmpty()) {
                val stored = readPrakIdBatches(ids) { field.schema.connection.getRetrievableReader().getAll(it).toList() }.associateBy { it.id }
                val emitted = mutableSetOf<UUID>()
                // Best-ranked keyframe represents each local segment; keep every mapped frame for later feedback.
                for ((position, entry) in matches.withIndex()) {
                    val (result, segmentIds) = entry
                    for (id in segmentIds) {
                        val segment = stored[id] ?: continue
                        if (!emitted.add(id)) continue
                        val frames = framesBySegment.getValue(id)
                        val score = if (feedback == null) result.scoreDouble else 1.0 - position.toDouble() / results.size
                        val properties = PrakResultAttribute(result.uri, position, result.scoreDouble, result.id, result.label, result.time)
                            .toPropertyAttribute().properties + mapOf(
                                "prak.frames" to PrakProtocol.json.encodeToString(frames),
                                "prak.ranking" to if (feedback == null) "similarity" else "bayes") +
                            if (emitted.size == 1) mapOf("prak.candidates" to PrakProtocol.json.encodeToString(results)) else emptyMap()
                        emit(segment.copy(attributes = segment.attributes.filterNot { it is ScoreAttribute || it is PropertyAttribute }.toSet() +
                            ScoreAttribute.Similarity(score) + PropertyAttribute(segment.attributes.filterIsInstance<PropertyAttribute>()
                                .flatMap { it.properties.entries }.associate { it.key to it.value } + properties), transient = false))
                    }
                }
            }
        } finally {
            client.close()
        }
    }
}
