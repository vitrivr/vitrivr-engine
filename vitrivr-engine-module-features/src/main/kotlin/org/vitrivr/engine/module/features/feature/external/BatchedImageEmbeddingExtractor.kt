package org.vitrivr.engine.module.features.feature.external

import io.ktor.client.plugins.retry
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import java.util.UUID

/** Batches image embeddings across retrievables and keeps descriptors associated with their original input. */
abstract class BatchedImageEmbeddingExtractor(
    input: Operator<out Retrievable>,
    analyser: Analyser<ContentElement<*>, FloatVectorDescriptor>,
    field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>?,
    name: String,
    private val host: String,
    route: String,
    private val batchSize: Int
) : AbstractExtractor<ContentElement<*>, FloatVectorDescriptor>(input, analyser, field, name) {
    private val endpoint = "${host.trimEnd('/')}/extract/${route}_image_batch"
    private val dimension by lazy { field?.getPrototype()?.vector?.value?.size }

    init {
        require(batchSize in 1..32) { "batchSize must be between 1 and 32." }
    }

    protected abstract fun embed(image: ImageContent, host: String): FloatVectorDescriptor

    final override fun extract(retrievable: Retrievable) = retrievable.content.filterIsInstance<ImageContent>().map { image ->
        embed(image, host).copy(retrievableId = retrievable.id, field = field)
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = if (batchSize == 1) {
        super.toFlow(scope)
    } else flow {
        val pending = ArrayList<Retrievable>(batchSize)
        suspend fun flush() {
            if (pending.isEmpty()) return
            val descriptions = extractBatch(pending)
            val descriptors = descriptions.flatten()
            if (descriptors.isNotEmpty()) writer?.addAll(descriptors)
            pending.zip(descriptions).forEach { (retrievable, result) -> emit(appendDescriptors(retrievable, result)) }
            pending.clear()
        }

        input.toFlow(scope).collect { retrievable ->
            if (retrievable == TerminalRetrievable) {
                flush()
                emit(retrievable)
            } else {
                pending.add(retrievable)
                if (pending.size == batchSize) flush()
            }
        }
        flush()
    }

    private suspend fun extractBatch(items: List<Retrievable>): List<List<FloatVectorDescriptor>> {
        val images = items.flatMapIndexed { index, item -> item.content.filterIsInstance<ImageContent>().map { index to it } }
        val results = List(items.size) { mutableListOf<FloatVectorDescriptor>() }
        try {
            for (batch in images.chunked(batchSize)) {
                val payload = Json.encodeToString(mapOf("images" to batch.map { (_, image) -> image.toDataUrl() }))
                val response = ExternalAnalyser.httpClient.post(endpoint) {
                    retry { noRetry() }
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                check(response.status.isSuccess()) { "Batch endpoint returned HTTP ${response.status.value}." }
                val vectors = Json.decodeFromString<List<FloatArray>>(response.bodyAsText())
                check(vectors.size == batch.size && vectors.all { vector ->
                    vector.isNotEmpty() && vector.all(Float::isFinite) && (dimension == null || vector.size == dimension)
                }) { "Batch endpoint returned invalid embeddings." }
                batch.zip(vectors).forEach { (entry, vector) ->
                    val index = entry.first
                    results[index].add(FloatVectorDescriptor(UUID.randomUUID(), items[index].id, Value.FloatVector(vector), field))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Image batch extraction failed for $name. Retrying with single-image requests." }
            return items.map { item ->
                try {
                    extract(item)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error(e) { "Could not extract $name for ${item.id}." }
                    emptyList()
                }
            }
        }
        return results
    }
}
