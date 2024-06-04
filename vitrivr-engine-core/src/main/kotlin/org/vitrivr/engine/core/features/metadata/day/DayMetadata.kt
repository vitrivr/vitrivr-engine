package org.vitrivr.engine.core.features.metadata.day

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.DayMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import kotlin.reflect.KClass

class DayMetadata: Analyser<ContentElement<*>, DayMetadataDescriptor> {
    /** The [KClass]es of the [ContentElement] accepted by this [Analyser].  */
    override val contentClasses = setOf(ContentElement::class)

    /** The [KClass] of the [Descriptor] generated by this [Analyser].  */
    override val descriptorClass = DayMetadataDescriptor::class

    /**
     * Generates a specimen of the [Descriptor] produced / consumed by this [Analyser] given the provided [Schema.Field]
     * This is a required operation!
     *
     * @param field The [Schema.Field] to create prototype for. Mainly used to support [Analyser]s with descriptors that depend on the [Schema.Field] configuration.ß
     * @return A [Descriptor] specimen of type [D].
     */
    override fun prototype(field: Schema.Field<*, *>) = DayMetadataDescriptor.PROTOTYPE

    /**
     * Creates a new [Extractor] instance from this [ExtractorFactory].
     *
     * @param name The name of the [Operator].
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, DayMetadataDescriptor> {
        throw UnsupportedOperationException("Not supported")
    }

    /**
     * Creates a new [Extractor] instance from this [ExtractorFactory].
     *
     * @param field The [Schema.Field] to create the [Extractor] for.
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, DayMetadataDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, DayMetadataDescriptor> {
        throw UnsupportedOperationException("Not supported")
    }

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser].
     *
     * This is the base-case, every [Analyser] should support this operation unless the [Analyser] is not meant to be used for retrieval at all,
     * in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, DayMetadataDescriptor>,
        query: Query,
        context: QueryContext
    ): Retriever<ContentElement<*>, DayMetadataDescriptor> {
        require(field.analyser == this){"Field type is incompatible with analyser. This is a programmer's error!"}
        require(query is BooleanQuery) {"Query is not a Boolean query."}
        return DayMetadataRetriever(field, query, context)
    }
}
