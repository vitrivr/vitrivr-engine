package org.vitrivr.engine.module.features.feature.lsc.coordinates

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import java.util.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

/**
 * Implementation of the [PostGISCoordinates] [Analyser], which derives GPS coordinates from an [ImageContent]
 * and stores them as a single PostGIS-compatible geography value (POINT WKT) in an [AnyMapStructDescriptor].
 *
 * @author henrikluemkemann
 * @version 1.1.0
 */
class PostGISCoordinates : Analyser<ImageContent, AnyMapStructDescriptor> {

    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
        const val DEFAULT_QUERY_LIMIT = 1000L
    }

    override val contentClasses: Set<KClass<out ImageContent>> = setOf(ImageContent::class)
    override val descriptorClass = AnyMapStructDescriptor::class

    /**
     * Creates the layout for the [AnyMapStructDescriptor] produced by this [PostGISCoordinates] analyser.
     * This analyser produces a single geography attribute with the name taken from the field.
     * 
     * @param fieldName The name of the field to create the layout for.
     * @return A list containing a single [Attribute] with the given name and Geography type.
     */
    private fun getLayoutForField(fieldName: String): List<Attribute> {
        return listOf(Attribute(name = fieldName, type = Type.Geography, nullable = false))
    }

    /**
     * Generates a prototypical [AnyMapStructDescriptor] for this [PostGISCoordinates].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [AnyMapStructDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): AnyMapStructDescriptor {
        val attributeName = field.fieldName
        val layout = getLayoutForField(attributeName)
        val defaultValues = mapOf<String, Value<*>?>(attributeName to Type.Geography.defaultValue())

        return AnyMapStructDescriptor(
            id = UUID.nameUUIDFromBytes("prototype-$attributeName".toByteArray()) as DescriptorId,
            retrievableId = null,
            layout = layout,
            values = defaultValues,
            field = field as? Schema.Field<*, AnyMapStructDescriptor>
        )
    }

    /**
     * Generates and returns a new [PostGISCoordinatesExtractor] instance for this [PostGISCoordinates].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     *
     */
    override fun newExtractor(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, AnyMapStructDescriptor> = PostGISCoordinatesExtractor(input, this, field)


    /**
     * Generates and returns a new [PostGISCoordinatesExtractor] instance for this [PostGISCoordinates].
     *
     * @param name The name of the [PostGISCoordinatesExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, AnyMapStructDescriptor> = PostGISCoordinatesExtractor(input, this, name)


    /**
     * Generates and returns a new [Retriever] instance for this [PostGISCoordinates].
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException]
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        query: Query,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> {
        val targetAttributeName = field.fieldName

        return when (query) {
            is SimpleBooleanQuery<*> -> {
                // The SimpleBooleanQuery's attributeName should match our targetAttributeName
                if (query.attributeName == targetAttributeName &&
                    (query.comparison == ComparisonOperator.EQ ||
                            query.comparison == ComparisonOperator.NEQ ||
                            query.comparison == ComparisonOperator.LIKE)
                ) {
                    logger.debug { "Creating StructBooleanRetriever for field '$targetAttributeName' (Geography as Text) with SimpleBooleanQuery (Op: ${query.comparison})." }
                    StructBooleanRetriever(field, query, context) // 'query' is already a BooleanQuery
                } else {
                    throw UnsupportedOperationException(
                        "SimpleBooleanQuery for field '$targetAttributeName' (Geography type) only supports EQ, NEQ, LIKE " +
                                "when targeting the field's primary geography attribute ('${query.attributeName}'). Operator: ${query.comparison}."
                    )
                }
            }
            else -> throw UnsupportedOperationException("Query type ${query::class.simpleName} not supported by PostGISCoordinates for field '$targetAttributeName'.")
        }
    }

    /**
     * Generates and returns a new [Retriever] instance for this [PostGISCoordinates].
     *
     * Invoking this method involves converting the provided [AnyMapStructDescriptor] into a [SimpleBooleanQuery] that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param descriptors A collection of [AnyMapStructDescriptor] elements to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     * @throws IllegalArgumentException If the collection of descriptors is empty or if the descriptor does not contain a value.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        descriptors: Collection<AnyMapStructDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(descriptors.isNotEmpty()) { "At least one descriptor must be provided." }

        val exampleDescriptor = descriptors.first()
        val geographyAttributeName = field.fieldName

        val geographyValue = exampleDescriptor.values()[geographyAttributeName] as? Value.GeographyValue
            ?: throw IllegalArgumentException("Example descriptor for field '$geographyAttributeName' is missing a valid geography value for attribute '$geographyAttributeName'. Values: ${exampleDescriptor.values()}")

        // Create a SimpleBooleanQuery for exact WKT match.
        val wktEqualityQuery = SimpleBooleanQuery(
            value = geographyValue,
            comparison = ComparisonOperator.EQ,
            attributeName = geographyAttributeName,
            limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: DEFAULT_QUERY_LIMIT
        )

        logger.debug { "Creating retriever for descriptors for field '$geographyAttributeName' using WKT equality query." }
        return newRetrieverForQuery(field, wktEqualityQuery, context)
        // TODO: change to GeographicProximityQuery!
    }

    /**
     * Generates and returns a new [Retriever] instance for this [PostGISCoordinates].
     *
     * Invoking this method involves converting the provided [ImageContent] and the [QueryContext] into an [AnyMapStructDescriptor]
     * that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param content A collection of [ImageContent] elements to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        content: Collection<ImageContent>,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> = newRetrieverForDescriptors(field, content.mapNotNull { analyse(it, field) }, context)


    /**
     * Performs the [PostGISCoordinates] analysis on the provided [ImageContent] element.
     *
     * Note: During query time, we only have access to the BufferedImage content, not the original file.
     * BufferedImage doesn't preserve EXIF metadata, so we can't extract GPS coordinates during query time.
     * This method is called during query time by newRetrieverForContent, and the metadata is already lost.
     *
     * @param content The [ImageContent] element to analyze.
     * @param fieldForContext The [Schema.Field] to create the descriptor for.
     * @throws [UnsupportedOperationException]
     */
    fun analyse(content: ImageContent, fieldForContext: Schema.Field<ImageContent, AnyMapStructDescriptor>): AnyMapStructDescriptor? {
        val operationDescription = "Cannot extract EXIF metadata for PostGISCoordinates during query time. " +
        "Metadata is lost when the image is loaded into memory (ImageContent)."

        logger.warn { "PostGISCoordinates.analyse(): $operationDescription" }

        throw UnsupportedOperationException(operationDescription) as Throwable
    }
}
