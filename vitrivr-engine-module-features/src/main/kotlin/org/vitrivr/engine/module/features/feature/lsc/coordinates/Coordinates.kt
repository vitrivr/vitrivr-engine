package org.vitrivr.engine.module.features.feature.lsc.coordinates

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import org.vitrivr.engine.core.features.capturetime.logger
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.bool.SpatialBooleanQuery
import org.vitrivr.engine.core.model.query.spatiotemporal.SpatialOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.UUID

/**
 * Implementation of the [Coordinates] [org.vitrivr.engine.core.model.metamodel.Analyser], which derives GPS coordinates from an [org.vitrivr.engine.core.model.content.element.ImageContent] as [org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor].
 *
 * @author henrikluemkemann
 * @version 1.1.0
 */
class Coordinates :
    Analyser<ImageContent, AnyMapStructDescriptor> {
    override val contentClasses = setOf(
        ImageContent::class)
    override val descriptorClass = AnyMapStructDescriptor::class

    /** The layout of the [AnyMapStructDescriptor] produced by this [Coordinates] analyser. */
    private val layout = listOf(
        Attribute(
            "lat",
            Type.Double
        ),
        Attribute(
            "lon",
            Type.Double
        )
    )

    /**
     * Generates a prototypical [AnyMapStructDescriptor] for this [Coordinates].
     *
     * @param field [org.vitrivr.engine.core.model.metamodel.Schema.Field] to create the prototype for.
     * @return [AnyMapStructDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) =
        AnyMapStructDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            this.layout,
            mapOf(
                "lat" to null,
                "lon" to null
            ),
            null
        )

    /**
     * Generates and returns a new [CoordinatesExtractor] instance for this [Coordinates].
     *
     * @param field The [Schema.Field] to create an [org.vitrivr.engine.core.operators.ingest.Extractor] for.
     * @param input The [org.vitrivr.engine.core.operators.Operator] that acts as input to the new [org.vitrivr.engine.core.operators.ingest.Extractor].
     * @param context The [org.vitrivr.engine.core.context.IndexContext] to use with the [org.vitrivr.engine.core.operators.ingest.Extractor].
     *
     * @return A new [org.vitrivr.engine.core.operators.ingest.Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [org.vitrivr.engine.core.operators.ingest.Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ImageContent, AnyMapStructDescriptor>, input: Operator<Retrievable>, context: IndexContext): Extractor<ImageContent, AnyMapStructDescriptor> =
        CoordinatesExtractor(
            input,
            this,
            field
        )

    /**
     * Generates and returns a new [CoordinatesExtractor] instance for this [Coordinates].
     *
     * @param name The name of the [CoordinatesExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ImageContent, AnyMapStructDescriptor> =
        CoordinatesExtractor(
            input,
            this,
            name
        )

    /**
     * Generates and returns a new [org.vitrivr.engine.core.features.bool.StructBooleanRetriever] instance for this [Coordinates].
     *
     * @param field The [Schema.Field] to create an [org.vitrivr.engine.core.operators.retrieve.Retriever] for.
     * @param query The [org.vitrivr.engine.core.model.query.Query] to use with the [org.vitrivr.engine.core.operators.retrieve.Retriever].
     * @param context The [org.vitrivr.engine.core.context.QueryContext] to use with the [org.vitrivr.engine.core.operators.retrieve.Retriever].
     *
     * @return A new [org.vitrivr.engine.core.operators.retrieve.Retriever] instance for this [Analyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, AnyMapStructDescriptor>, query: Query, context: QueryContext): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(query is BooleanQuery) { "The query is not a BooleanQuery." }
        return StructBooleanRetriever(
            field,
            query,
            context
        )
    }


    /**
     * Generates and returns a new [Retriever] that finds items with coordinates
     * near the location specified in the provided example descriptor.
     *
     * This method creates a [SpatialBooleanQuery] that performs a proximity search
     * (e.g., within a 500-meter radius) around the coordinates of the example descriptor.
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param descriptors A collection of [AnyMapStructDescriptor] elements to use as the example.
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     * @throws IllegalArgumentException If the collection of descriptors is empty or if the descriptor
     * does not contain valid 'lat' and 'lon' values.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        descriptors: Collection<AnyMapStructDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(descriptors.isNotEmpty()) { "At least one descriptor must be provided." }

        val descriptor = descriptors.first()
        val lat = (descriptor.values()["lat"] as? Value.Double)?.value
            ?: throw IllegalArgumentException("Descriptor is missing a valid 'lat' attribute.")
        val lon = (descriptor.values()["lon"] as? Value.Double)?.value
            ?: throw IllegalArgumentException("Descriptor is missing a valid 'lon' attribute.")

        val radiusInMeters = context.getProperty(field.fieldName, "radius")?.toDoubleOrNull() ?: 500.0
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L

        val centerPointWkt = "POINT($lon $lat)"

        val spatialQuery =
            SpatialBooleanQuery(
                latAttribute = "lat",
                lonAttribute = "lon",
                operator = SpatialOperator.DWITHIN,
                reference = Value.GeographyValue(centerPointWkt),
                distance = Value.Double(radiusInMeters),
                limit = limit
            )

        return newRetrieverForQuery(field, spatialQuery, context)
    }

    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [Coordinates].
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
     * Performs the [Coordinates] analysis on the provided [ImageContent] element.
     *
     * Note: During query time, we only have access to the BufferedImage content, not the original file.
     * BufferedImage doesn't preserve EXIF metadata, so we can't extract GPS coordinates during query time.
     * This method is called during query time by newRetrieverForContent, and the metadata is already lost.
     *
     * @param content The [ImageContent] element to analyze.
     * @return [AnyMapStructDescriptor] containing the GPS coordinates information, or an empty descriptor if metadata cannot be extracted.
     */
    fun analyse(content: ImageContent): AnyMapStructDescriptor {
        val operationDescription = "Cannot extract EXIF metadata for coordinates during query time. " +
                "Metadata is lost when the image is loaded into memory (ImageContent)."

        logger.warn { "Coordinates.analyse(): $operationDescription" }

        throw UnsupportedOperationException(operationDescription) as Throwable
    }
}