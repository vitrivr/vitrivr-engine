package org.vitrivr.engine.module.features.feature.lsc.timestamp

import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.UUID

/**
 *
 * This analyser relies on [LSCTimestampExtractor] to parse the 'minute_id' from image metadata.
 * The extracted date is of type [java.time.LocalDateTime] and is stored using [Value.DateTime].
 *
 * @author henrikluemkemann
 * @version 1.0.1
 */
class LSCTimestamp :
    Analyser<ImageContent, AnyMapStructDescriptor> {

    private val logger = KotlinLogging.logger {}

    companion object {
        /** The public name of this feature, used for schema registration. */
        const val FEATURE_NAME = "LSCTimestamp"
        /** The internal attribute name used within descriptors for storing the extracted date. */
        internal const val ATTRIBUTE_NAME = "minuteIdTimestamp"
    }

    override val contentClasses = setOf(
        ImageContent::class)
    override val descriptorClass = AnyMapStructDescriptor::class

    /**
     * Defines the structure of the descriptor: an attribute named [ATTRIBUTE_NAME]
     * of type [Type.Datetime], which will hold a [java.time.LocalDateTime].
     */
    private val layout = listOf(
        Attribute(
            ATTRIBUTE_NAME,
            Type.Datetime
        )
    )

    /**
     * Generates a prototypical [AnyMapStructDescriptor] for this [LSCTimestamp].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [AnyMapStructDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): AnyMapStructDescriptor =
        AnyMapStructDescriptor(
            id = UUID.randomUUID(),
            retrievableId = UUID.randomUUID(),
            layout = this.layout,
            values = mapOf(
                ATTRIBUTE_NAME to Type.Datetime.defaultValue()
            ),
            field = null
        )

    /**
     * Creates a new [LSCTimestampExtractor] for the given field.
     *
     * **Note**: This assumes that the [LSCTimestampExtractor]'s constructor for the 'analyser'
     * parameter is typed as `Analyser<ImageContent, AnyMapStructDescriptor>` or a compatible interface,
     * not a concrete class.
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     *
     */
    override fun newExtractor(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, AnyMapStructDescriptor> =
        LSCTimestampExtractor(
            input,
            this,
            field
        )

    /**
     * Creates a new [LSCTimestampExtractor] for the given name.
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, AnyMapStructDescriptor> =
        LSCTimestampExtractor(
            input,
            this,
            name
        )


    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [LSCTimestamp],
     * using the provided [BooleanQuery].
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        query: Query,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(query is BooleanQuery) { "Query must be of type BooleanQuery for $FEATURE_NAME retriever." }

        return StructBooleanRetriever(
            field,
            query,
            context
        )
    }


    /**
     * Generates and returns a new [StructBooleanRetriever] based on the provided descriptors.
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param descriptors A collection of [AnyMapStructDescriptor] elements to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     * @throws IllegalArgumentException If the collection is empty or the descriptor does not contain a valid timestamp.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        descriptors: Collection<AnyMapStructDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, AnyMapStructDescriptor> {
        require(descriptors.isNotEmpty()) { "At least one descriptor must be provided for $FEATURE_NAME." }

        val firstDescriptorValues = descriptors.first().values()
        val timestampValueWrapper = firstDescriptorValues[ATTRIBUTE_NAME] // This is Value<*>?
            ?: throw IllegalArgumentException("Descriptor for $FEATURE_NAME does not contain '$ATTRIBUTE_NAME' value.")


        val specificValueDateTime: Value.DateTime = timestampValueWrapper as? Value.DateTime
            ?: throw IllegalArgumentException(
                "'$ATTRIBUTE_NAME' value in descriptor is not of type Value.DateTime. " +
                        "Actual value: '$timestampValueWrapper' (type: ${timestampValueWrapper::class.simpleName})."
            )

        val query =
            SimpleBooleanQuery<Value.DateTime>(
                value = specificValueDateTime,
                comparison = ComparisonOperator.EQ,
                attributeName = ATTRIBUTE_NAME
            )

        return newRetrieverForQuery(field, query, context)
    }


    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [LSCTimestamp],
     * based on the provided [ImageContent].
     *
     * Note: Actual timestamp extraction cannot be performed at query time due to metadata loss.
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param content A collection of [ImageContent] elements to analyze and use for retrieval.
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser].
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>,
        content: Collection<ImageContent>,
        context: QueryContext
    ) = newRetrieverForDescriptors(field, content.map { analyse(it) }, context)

    /**
     * Performs analysis on the [ImageContent] to derive the LSC timestamp.
     *
     * @param content The [ImageContent] element to analyze.
     * @throws [UnsupportedOperationException]
     */
    fun analyse(content: ImageContent): AnyMapStructDescriptor {
        val operationDescription = "Cannot extract something from EXIF metadata during query time. " +
                "Metadata is lost when the image is loaded into memory (ImageContent)."

        logger.warn { "LSCTimestamp.analyse(): $operationDescription" }

        throw UnsupportedOperationException(operationDescription) as Throwable
    }
}