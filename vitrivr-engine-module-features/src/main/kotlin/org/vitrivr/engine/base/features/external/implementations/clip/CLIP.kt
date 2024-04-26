package org.vitrivr.engine.base.features.external.implementations.clip

import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.base.features.external.implementations.dino.DINORetriever
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [CLIP] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] or [TextContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.2.0
 */
class CLIP : ExternalAnalyser<ContentElement<*>,FloatVectorDescriptor>() {

    companion object {
        /**
         * Requests the CLIP feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the CLIP feature descriptor.
         * @param hostname The hostname of the external feature descriptor service.
         * @return A list of CLIP feature descriptors.
         */
        fun analyse(content: ContentElement<*>, hostname: String): FloatVectorDescriptor = when (content) {
            is ImageContent -> httpRequest(content, "$hostname/extract/clip_image") ?: throw IllegalArgumentException("Failed to generate CLIP descriptor.")
            is TextContent -> httpRequest(content, "$hostname/extract/clip_text")  ?: throw IllegalArgumentException("Failed to generate CLIP descriptor.")
            else -> throw IllegalArgumentException("Content '$content' not supported")
        }
    }


    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIP].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(512) { Value.Float(0.0f) })

    /**
     * Generates and returns a new [Extractor] instance for this [CLIP].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>?, input: Operator<Ingested>, context: IndexContext)
        = CLIPExtractor(input, field, context)

    /**
     * Generates and returns a new [Retriever] instance for this [CLIP].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: Query, context: QueryContext): CLIPRetriever {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value.first() is Value.Float) { "The query is not a ProximityQuery<Value.Float>." }
        @Suppress("UNCHECKED_CAST")
        return CLIPRetriever(field, query as ProximityQuery<Value.Float>, context)
    }

    /**
     * Generates and returns a new [DINORetriever] instance for this [CLIP].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): CLIPRetriever {
        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = descriptors.first().vector, k = k, distance = Distance.COSINE, fetchVector = fetchVector), context)
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

        /* Extract vectors from content. */
        val vectors = content.map { analyse(it, host) }

        /* Return retriever. */
        return this.newRetrieverForDescriptors(field, vectors, context)
    }
}