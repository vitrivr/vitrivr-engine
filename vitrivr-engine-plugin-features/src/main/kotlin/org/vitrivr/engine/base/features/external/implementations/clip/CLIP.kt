package org.vitrivr.engine.base.features.external.implementations.clip

import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [CLIP] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] or [TextContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIP : ExternalWithFloatVectorDescriptorAnalyser<ContentElement<*>>() {
    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIP].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*,*>) = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(512) { 0.0f }, true)

    /**
     * Generates and returns a new [Extractor] instance for this [CLIP].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return CLIPExtractor(input, field, persisting)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CLIP].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return this.newRetrieverForDescriptors(field, content.map { this.analyse(it, host) }, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CLIP].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return CLIPRetriever(field, descriptors.first(), context)
    }

    /**
     * Requests the CLIP feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CLIP feature descriptor.
     * @param hostname The hostname of the external feature descriptor service.
     * @return A list of CLIP feature descriptors.
     */
    override fun analyse(content: ContentElement<*>, hostname: String): FloatVectorDescriptor = when (content) {
        is ImageContent -> FloatVectorDescriptor(UUID.randomUUID(), null, httpRequest(content, "$hostname/extract/clip_image"), true)
        is TextContent -> FloatVectorDescriptor(UUID.randomUUID(), null, httpRequest(content, "$hostname/extract/clip_text"), true)
        else -> throw IllegalArgumentException("Content '$content' not supported")
    }
}