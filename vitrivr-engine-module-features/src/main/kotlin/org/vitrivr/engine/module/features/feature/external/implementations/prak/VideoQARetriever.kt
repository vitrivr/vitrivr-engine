package org.vitrivr.engine.module.features.feature.external.implementations.prak

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID

/** Emits one transient answer, without reading or writing local descriptors. */
class VideoQARetriever(
    field: Schema.Field<ContentElement<*>, TextDescriptor>,
    private val question: String,
    context: Context
) : AbstractRetriever<ContentElement<*>, TextDescriptor>(field,
    SimpleBooleanQuery(value = Value.Text(question), attributeName = field.fieldName, limit = 1), context) {

    private fun parameter(name: String) = context[field.fieldName, name] ?: field.parameters[name]

    internal fun request(): VideoQARequest = VideoQARequest(
        question = question,
        media = parameter("media")?.let { Json.decodeFromString<VideoQAMedia>(it) } ?: VideoQAMedia(),
        videoId = parameter("video_id")?.let { Json.decodeFromString<List<String>>(it) }.orEmpty(),
        cameraFilter = parameter("camera_filter")?.let { Json.decodeFromString<List<String>>(it) }.orEmpty(),
        timeStart = parameter("time_start"),
        timeEnd = parameter("time_end"),
        additionalFilters = parameter("additional_filters")?.let { Json.decodeFromString<List<VideoQAFilter>>(it) }.orEmpty()
    ).also { it.validate() }

    override fun toFlow(scope: CoroutineScope) = flow {
        val endpoint = parameter("endpoint") ?: throw IllegalArgumentException("VideoQA requires an endpoint parameter.")
        val request = request()
        val answer = withContext(Dispatchers.IO) { VideoQAClient(endpoint).answer(request) }
        emit(Retrieved(UUID.randomUUID(), "TEXT", content = listOf(InMemoryTextContent(answer)),
            attributes = setOf(PropertyAttribute(mapOf("qa.answer" to answer, "qa.question" to question))), transient = true))
    }
}
