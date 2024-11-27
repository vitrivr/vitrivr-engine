package org.vitrivr.engine.module.features.feature.external.implementations.dino

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.BoundedCorrespondence
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityPredicate
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.CONTENT_AUTHORS_KEY
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import java.util.*

/**
 * Implementation of the [DINO] [ExternalAnalyser], which derives the DINO feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.1.0
 */
class DINO : ExternalAnalyser<ImageContent, FloatVectorDescriptor>() {

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    companion object {
        /**
         * Requests the DINO feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the CLIP feature descriptor.
         * @param hostname The hostname of the external feature descriptor service.
         * @return DINO feature descriptor.
         */
        fun analyse(content: ContentElement<*>, hostname: String): FloatVectorDescriptor {
            val requestBody = when (content) {
                is ImageContent -> content.toDataUrl()
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }
            val url = "$hostname/extract/dino"
            return httpRequest<FloatVectorDescriptor>(url, requestBody)
                ?: throw IllegalArgumentException("Failed to generate DINO descriptor.")
        }
    }

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DINO].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(384))

    /**
     * Generates and returns a new [Extractor] instance for this [DINO].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @return [DINOExtractor]
     */
    override fun newExtractor(field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext): DINOExtractor {
        val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return DINOExtractor(input, this, context[field.fieldName, CONTENT_AUTHORS_KEY]?.split(",")?.toSet(), field, host)
    }

    /**
     * Generates and returns a new [Extractor] instance for this [DINO].
     *
     * @param name The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @return [DINOExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): DINOExtractor {
        val host: String = context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        return DINOExtractor(input, this, context[name, CONTENT_AUTHORS_KEY]?.split(",")?.toSet(), name, host)
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [DINO].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query An array of [Query] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: Query, context: QueryContext) = DenseRetriever(field, query, context, BoundedCorrespondence(0.0f, 2.0f))

    /**
     * Generates and returns a new [DenseRetriever] instance for this [DINO].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): DenseRetriever<ImageContent> {
        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() == true

        /* Return retriever. */
        val predicate = ProximityPredicate(field = field, value = descriptors.first().vector, k = k, fetchVector = fetchVector)
        return this.newRetrieverForQuery(field, Query(predicate), context)
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [DINO].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>, context: QueryContext): DenseRetriever<ImageContent> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

        /* Extract vectors from content. */
        val vectors = content.map { analyse(it, host) }

        /* Return retriever. */
        return this.newRetrieverForDescriptors(field, vectors, context)
    }
}