package org.vitrivr.engine.core.features.metadata.source.video

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadata
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * Implementation of the [VideoSourceMetadata] [Analyser], which derives metadata information from a [Retrievable].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoSourceMetadata : Analyser<ContentElement<*>, VideoSourceMetadataDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = VideoSourceMetadataDescriptor::class

    /**
     * Generates a prototypical [FileSourceMetadataDescriptor] for this [FileSourceMetadata].
     *
     * @param field The [Schema.Field] to create the prototype for.
     * @return [FileSourceMetadataDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = VideoSourceMetadataDescriptor.PROTOTYPE

    /**
     * Generates and returns a new [FileSourceMetadataExtractor] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileSourceMetadataExtractor]. Can be null.
     * @param input The input [Operator]
     * @param context The [IndexContext]
     *
     * @return [FileSourceMetadataExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, input: Operator<Retrievable>, context: IndexContext) = VideoSourceMetadataExtractor(input, this, field)

    /**
     * Generates and returns a new [FileSourceMetadataExtractor] for the provided [Schema.Field].
     *
     * @param name The name of the [FileSourceMetadataExtractor].
     * @param input The input [Operator]
     * @param context The [IndexContext]
     *
     * @return [FileSourceMetadataExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, VideoSourceMetadataDescriptor> = VideoSourceMetadataExtractor(input, this, name)

    /**
     * Generates and returns a new [VideoSourceMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [VideoSourceMetadataRetriever].
     * @param query The [Query] to create [VideoSourceMetadataRetriever] for.
     * @param context The [QueryContext]
     *
     * @return [VideoSourceMetadataRetriever]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, query: Query, context: QueryContext): VideoSourceMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        require(query is BooleanQuery) { "Query is not a Boolean query." }
        return VideoSourceMetadataRetriever(field, query, context)
    }

    /**
     * [FileSourceMetadataRetriever] Cannot derive a [VideoSourceMetadataRetriever] from content.
     *
     * This method will always throw an [UnsupportedOperationException]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): VideoSourceMetadataRetriever {
        throw UnsupportedOperationException("FileSourceMetadata does not support the creation of a Retriever instance from content.")
    }
}