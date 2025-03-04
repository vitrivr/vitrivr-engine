package org.vitrivr.engine.module.features.feature.external.implementations.clip

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.metadata.temporal.TemporalMetadata
import org.vitrivr.engine.core.features.metadata.temporal.TemporalMetadataRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.struct.metadata.ShotBoundaryDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.CONTENT_AUTHORS_KEY
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import org.vitrivr.engine.module.features.feature.external.implementations.msb.MsbRetriever
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Implementation of the [CLIP] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] or [TextContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.3.0
 */
class Msb : ExternalAnalyser<ContentElement<*>, ShotBoundaryDescriptor>() {

    companion object {
        /**
         * Requests the MSB feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the MSB feature descriptor.
         * @param hostname The hostname of the external feature descriptor service.
         * @return A list of MSB feature descriptors.
         */
        fun analyse(content: ContentElement<*>, hostname: String): ShotBoundaryDescriptor {
            val requestBody = when (content) {
                is TextContent -> URLEncoder.encode(content.toDataUrl(), StandardCharsets.UTF_8.toString())
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }
            val url = when (content) {
                is TextContent -> "$hostname/extract/msb"
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }
            return httpRequest<ShotBoundaryDescriptor>(url, "data=$requestBody")
                ?: throw IllegalArgumentException("Failed to generate CLIP descriptor.")
        }
    }


    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = ShotBoundaryDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIP].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = ShotBoundaryDescriptor(
        UUID.randomUUID(),
        UUID.randomUUID(),
        mapOf("starts" to Value.String("[0,1000000000]"), "ends" to Value.String("[1000000000,2000000000]"))
    )

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
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, ShotBoundaryDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): MsbExtractor {
        val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return MsbExtractor(
            input,
            this,
            context[field.fieldName, CONTENT_AUTHORS_KEY]?.split(",")?.toSet(),
            field,
            host
        )
    }

    /**
     * Generates and returns a new [Extractor] instance for this [CLIP].
     *
     * @param name The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): MsbExtractor {
        val host: String = context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        return MsbExtractor(input, this, context[name, CONTENT_AUTHORS_KEY]?.split(",")?.toSet(), name, host)
    }

    /**
     * Generates and returns a new [TemporalMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [TemporalMetadataRetriever].
     * @param query The [Query] to create [TemporalMetadataRetriever] for.
     * @param context The [QueryContext]
     *
     * @return [TemporalMetadataRetriever]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, ShotBoundaryDescriptor>, query: Query, context: QueryContext): MsbRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        require(query is BooleanQuery) { "Query is not a Boolean query." }
        return MsbRetriever(field, query, context)
    }

    /**
     * [TemporalMetadata] cannot derive a [TemporalMetadataRetriever] from content.
     *
     * This method will always throw an [UnsupportedOperationException]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, ShotBoundaryDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): MsbRetriever {
        throw UnsupportedOperationException("TemporalMetadataDescriptor does not support the creation of a Retriever instance from content.")
    }
}