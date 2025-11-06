package org.vitrivr.engine.module.features.feature.external.implementations.emotions
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.math.correspondence.BoundedCorrespondence
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import java.net.URLEncoder
import java.util.*

class EMOTIONSSOUND : ExternalAnalyser<ContentElement<*>, FloatVectorDescriptor>(){


    companion object {
        /**
         * Requests Emotions classification for the given [AudioContent].
         *
         * @param content  The [AudioContent] for which to request transcription.
         * @param hostname The hostname of the external ASR service.
         * @return The extracted [VectorDescriptor].
         */
        fun analyse(content: ContentElement<*>, hostname: String): FloatVectorDescriptor {
            val audioDataUrl = when (content) {
                is AudioContent -> content.toDataURL()
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }

            val url = "$hostname/extract/emotions_sound"

            val encoded = URLEncoder.encode(audioDataUrl, Charsets.UTF_8.name())
            val response = httpRequest<FloatVectorDescriptor>(url, requestBody = "data=$encoded")
                ?: throw IllegalArgumentException("Failed to generate ASR descriptor.")
            return response
        }

    }

    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [EMOTIONSSOUND].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) =
        FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(7))

    /**
     * Generates and returns a new [Extractor] instance for this [EMOTIONSSOUND].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [Context] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [EMOTIONSSOUND]
     * @throws [UnsupportedOperationException], if this [EMOTIONSSOUND] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        input: Operator<out Retrievable>,
        context: Context
    ): EMOTIONSSOUNDExtractor {
        val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return EMOTIONSSOUNDExtractor(input, this, field, host)
    }

    /**
     * Generates and returns a new [Extractor] instance for this [EMOTIONSSOUND].
     *
     * @param name The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [Context] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [EMOTIONSSOUND]
     * @throws [UnsupportedOperationException], if this [EMOTIONSSOUND] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        name: String,
        input: Operator<out Retrievable>,
        context: Context
    ): EMOTIONSSOUNDExtractor {
        val host: String = context.getProperty(name,HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        return EMOTIONSSOUNDExtractor(input, this, name, host)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [EMOTIONSSOUND].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [EMOTIONSSOUND]
     * @throws [UnsupportedOperationException], if this [EMOTIONSSOUND] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        query: Query,
        context: Context
    ): DenseRetriever<ContentElement<*>> {
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(
            field,
            query as ProximityQuery<Value.FloatVector>,
            context,
            BoundedCorrespondence(0.0, 2.0)
        )
    }

    /**
     * Generates and returns a new [Retriever] instance for this [EMOTIONSSOUND].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [EMOTIONSSOUND]
     * @throws [UnsupportedOperationException], if this [EMOTIONSSOUND] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Map<String, ContentElement<*>>, context: Context): DenseRetriever<ContentElement<*>> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

        /* Extract vectors from content. */
        val vectors = content.values.map { EMOTIONSSOUND.Companion.analyse(it, host) }

        /* Return retriever. */
        return this.newRetrieverForDescriptors(field, vectors, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [EMOTIONSSOUND].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        descriptors: Collection<FloatVectorDescriptor>,
        context: Context
    ): DenseRetriever<ContentElement<*>> {
        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(
            field,
            ProximityQuery(
                value = descriptors.first().vector,
                k = k,
                distance = Distance.COSINE,
                fetchVector = fetchVector
            ),
            context
        )
    }
}
