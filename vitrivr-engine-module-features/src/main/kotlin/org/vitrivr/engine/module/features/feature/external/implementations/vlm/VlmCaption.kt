package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.struct.VlmDescriptionDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.io.IOException
import java.time.Duration
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Registers keyframe caption extraction and ranked full-text retrieval for a schema field.
 *
 * @author Rahel Arnold
 */
open class VlmCaption protected constructor(private val defaultPrompt: String) :
    Analyser<ContentElement<*>, VlmDescriptionDescriptor> {
    constructor() : this(DEFAULT_PROMPT)

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(ImageContent::class)
    override val descriptorClass = VlmDescriptionDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) =
        VlmDescriptionDescriptor(UUID(0, 0), null, emptyMap())

    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>,
        input: Operator<out Retrievable>,
        context: Context
    ): Extractor<ContentElement<*>, VlmDescriptionDescriptor> =
        extractor(field.fieldName, field, input, context.global + Analyser.merge(field, context), context)

    override fun newExtractor(
        name: String, input: Operator<out Retrievable>, context: Context
    ): Extractor<ContentElement<*>, VlmDescriptionDescriptor> =
        extractor(name, null, input, context.global + context.local[name].orEmpty(), context)

    private fun extractor(
        name: String,
        field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>?,
        input: Operator<out Retrievable>,
        parameters: Map<String, String>,
        context: Context
    ): VlmCaptionExtractor {
        val enabled = parameters["enabled"]?.toBooleanStrict() ?: true
        val concurrency = parameters["maxConcurrentRequests"]?.toInt() ?: 1
        val client = if (enabled) OpenAiVlmClient(
            baseUrl = requireNotNull(parameters["baseUrl"]) { "$name requires baseUrl." },
            model = requireNotNull(parameters["model"]) { "$name requires model." },
            prompt = parameters["prompt"] ?: defaultPrompt,
            promptVersion = parameters["promptVersion"] ?: "1",
            maxOutputTokens = parameters["maxOutputTokens"]?.toInt() ?: 128,
            timeout = Duration.ofMillis(parameters["requestTimeoutMs"]?.toLong() ?: 60_000),
            maxConcurrentRequests = concurrency,
            disableThinking = parameters["disableThinking"]?.toBooleanStrict() ?: false
        ) else null
        val describe = client?.let { descriptionSource(it, parameters, context) }
        return VlmCaptionExtractor(input, this, field, name, describe, concurrency)
    }

    protected open fun descriptionSource(
        client: OpenAiVlmClient, parameters: Map<String, String>, context: Context
    ): (Retrievable) -> VlmDescription? = describe@{ shot ->
        val images = shot.content.filterIsInstance<ImageContent>()
        if (images.isEmpty()) return@describe null
        val image = images.singleOrNull() ?: throw IOException(
            "Expected one representative keyframe for shot ${shot.id}. Found ${images.size}."
        )
        client.describe(image)
    }

    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>, query: Query, context: Context
    ): AbstractRetriever<ContentElement<*>, VlmDescriptionDescriptor> {
        require(query is SimpleFulltextQuery && query.value.value.isNotBlank()) { "VlmCaption requires a nonblank full-text query." }
        return object : AbstractRetriever<ContentElement<*>, VlmDescriptionDescriptor>(
            field, query.copy(attributeName = VlmDescriptionDescriptor.CAPTION, ranked = true), context
        ) {}
    }

    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>,
        content: Map<String, ContentElement<*>>,
        context: Context
    ): AbstractRetriever<ContentElement<*>, VlmDescriptionDescriptor> {
        val text = (content.values.singleOrNull() as? TextContent)?.text
        requireNotNull(text) { "VlmCaption retrieval requires one text input." }
        return retriever(field, Value.Text(text), context)
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>,
        descriptors: Collection<VlmDescriptionDescriptor>,
        context: Context
    ): AbstractRetriever<ContentElement<*>, VlmDescriptionDescriptor> {
        val text = descriptors.singleOrNull()?.values()?.get(VlmDescriptionDescriptor.CAPTION) as? Value.Text
        return retriever(field, requireNotNull(text) { "VlmCaption retrieval requires one caption." }, context)
    }

    private fun retriever(
        field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>, text: Value.Text, context: Context
    ): AbstractRetriever<ContentElement<*>, VlmDescriptionDescriptor> {
        val limit = (context.getProperty(field.fieldName, "limit") ?: field.parameters["limit"])?.toLong() ?: 1000L
        return newRetrieverForQuery(field, SimpleFulltextQuery(text, limit = limit), context)
    }

    private companion object {
        const val DEFAULT_PROMPT = "Describe the visible content of this image in one or two concise, factual sentences. " +
            "Mention important objects, people, actions, and spatial relationships. Describe only what is visually supported " +
            "by the image. Do not invent temporal events, intentions, or details outside the frame."
    }
}
