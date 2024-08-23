package org.vitrivr.engine.core.features.metadata.source.exif

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class ExifMetadata : Analyser<ContentElement<*>, AnyMapStructDescriptor> {

    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = AnyMapStructDescriptor::class
    override fun prototype(field: Schema.Field<*, *>): AnyMapStructDescriptor {
        val parameters = field.parameters.map { (k, v) -> Attribute(k, Type.valueOf(v)) }
        return AnyMapStructDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            parameters,
            parameters.associate { it.name to it.type.defaultValue() },
        )
    }

    /**
     * Generates and returns a new [ExifMetadataExtractor] instance for this [ExifMetadata].
     *
     * @param name The name of the [ExifMetadataExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = ExifMetadataExtractor(input, this, name)

    /**
     * Generates and returns a new [ExifMetadataExtractor] instance for this [ExifMetadata].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, AnyMapStructDescriptor>, input: Operator<Retrievable>, context: IndexContext) = ExifMetadataExtractor(input, this, field)

    /**
     *
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, AnyMapStructDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, AnyMapStructDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        require(query is SimpleBooleanQuery<*>) { "Query is not a Query." }
        return ExifMetadataRetriever(field, query, context)
    }

    /**
     *
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, AnyMapStructDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): ExifMetadataRetriever {
        throw UnsupportedOperationException("ExifMetadata does not support the creation of a Retriever instance from content.")
    }
}