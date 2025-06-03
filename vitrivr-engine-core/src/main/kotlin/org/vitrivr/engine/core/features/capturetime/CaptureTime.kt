package org.vitrivr.engine.core.features.capturetime

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*


/**
 * Implementation of the [CaptureTime] [Analyser].
 * It configures the [CaptureTimeExtractor] to derive the capture time from [ImageContent]
 * as a [java.time.LocalDateTime], which is then stored wrapped in a [org.vitrivr.engine.core.model.types.Value.DateTime] object.
 *
 * @author henrikluemkemann
 * @version 1.2.0
 */
class CaptureTime : Analyser<ImageContent, AnyMapStructDescriptor> {

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = AnyMapStructDescriptor::class

    private val layout = listOf(
        Attribute("timestamp", Type.Datetime)
    )

    /**
     * Generates a prototypical [AnyMapStructDescriptor] for this [CaptureTime].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [AnyMapStructDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = AnyMapStructDescriptor(
        UUID.randomUUID(),
        UUID.randomUUID(),
        this.layout,
        mapOf("timestamp" to null),
        null
    )

    /**
     * Generates and returns a new [CaptureTimeExtractor] instance for this [CaptureTime].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ImageContent, AnyMapStructDescriptor>, input: Operator<Retrievable>, context: IndexContext): Extractor<ImageContent, AnyMapStructDescriptor> = CaptureTimeExtractor(input, this, field)

    /**
     * Generates and returns a new [CaptureTimeExtractor] instance for this [CaptureTime].
     *
     * @param name The name of the [CaptureTimeExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ImageContent, AnyMapStructDescriptor> = CaptureTimeExtractor(input, this, name)

    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [CaptureTime].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, AnyMapStructDescriptor>, query: Query, context: QueryContext): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(query is BooleanQuery) { "The query is not a BooleanQuery." }
        return StructBooleanRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [CaptureTime].
     *
     * Invoking this method involves converting the provided [AnyMapStructDescriptor] into a [SimpleBooleanQuery] that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors A collection of [AnyMapStructDescriptor] elements to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     * @throws IllegalArgumentException If the collection of descriptors is empty or if the descriptor does not contain a 'timestamp' value.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        descriptors: Collection<AnyMapStructDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(descriptors.isNotEmpty()) { "At least one descriptor must be provided." }

        val timestamp = descriptors.first().values()["timestamp"]
            ?: throw IllegalArgumentException("Descriptor does not contain 'timestamp' value.")

        val query = SimpleBooleanQuery(timestamp, ComparisonOperator.EQ, "timestamp") // TODO find better default case
        return newRetrieverForQuery(field, query, context)
    }

    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [CaptureTime].
     *
     * Invoking this method involves converting the provided [ImageContent] and the [QueryContext] into an [AnyMapStructDescriptor]
     * that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content A collection of [ImageContent] elements to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        content: Collection<ImageContent>,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> = newRetrieverForDescriptors(field, content.map { analyse(it) }, context)

    /**
     * Performs the [CaptureTime] analysis on the provided [ImageContent] element.
     *
     * Note: During query time, we only have access to the BufferedImage content, not the original file.
     * BufferedImage doesn't preserve EXIF metadata, so we can't extract capture time during query time.
     * This method is called during query time by newRetrieverForContent, and the metadata is already lost.
     *
     * @param content The [ImageContent] element to analyze.
     * @return [AnyMapStructDescriptor] containing the capture time information, or an empty descriptor if metadata cannot be extracted.
     * @throws UnsupportedOperationException
     */
    fun analyse(content: ImageContent): AnyMapStructDescriptor {
        val operationDescription = "Cannot extract EXIF metadata for capture time during query time. " +
                "Metadata is lost when the image is loaded into memory (ImageContent)."

        logger.warn { "CaptureTime.analyse(): $operationDescription" }

        throw UnsupportedOperationException(operationDescription) as Throwable
    }

}
