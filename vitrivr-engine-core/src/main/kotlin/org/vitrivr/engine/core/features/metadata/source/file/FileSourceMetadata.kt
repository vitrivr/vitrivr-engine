package org.vitrivr.engine.core.features.metadata.source.file

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * Implementation of the [FileSourceMetadata] [Analyser], which derives metadata information from file-based retrievables
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class FileSourceMetadata : Analyser<ContentElement<*>, FileSourceMetadataDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = FileSourceMetadataDescriptor::class

    /**
     * Generates a prototypical [FileSourceMetadataDescriptor] for this [FileSourceMetadata].
     *
     *  @param field The [Schema.Field] to create the prototype for.
     * @return [FileSourceMetadataDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = FileSourceMetadataDescriptor.PROTOTYPE

    /**
     * Generates and returns a new [FileSourceMetadataExtractor] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileSourceMetadataExtractor]. Can be null.
     * @param input The input [Operator]
     * @param context The [IndexContext]
     *
     * @return [FileSourceMetadataExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, input: Operator<Retrievable>, context: IndexContext) = FileSourceMetadataExtractor(input, this, field)

    /**
     * Generates and returns a new [FileSourceMetadataExtractor] for the provided [Schema.Field].
     *
     * @param name The name of the [FileSourceMetadataExtractor].
     * @param input The input [Operator]
     * @param context The [IndexContext]
     *
     * @return [FileSourceMetadataExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = FileSourceMetadataExtractor(input, this, name)

    /**
     * Generates and returns a new [FileSourceMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileSourceMetadataRetriever].
     * @param query The [Query] to create [FileSourceMetadataRetriever] for.
     * @param context The [QueryContext]
     *
     * @return [FileSourceMetadataRetriever]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, FileSourceMetadataDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        require(query is SimpleBooleanQuery<*>) { "Query is not a Query." }
        return FileSourceMetadataRetriever(field, query, context)
    }

    /**
     * [FileSourceMetadataRetriever] Cannot derive a [FileSourceMetadataRetriever] from content.
     *
     * This method will always throw an [UnsupportedOperationException
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): FileSourceMetadataRetriever {
        throw UnsupportedOperationException("FileSourceMetadata does not support the creation of a Retriever instance from content.")
    }
}