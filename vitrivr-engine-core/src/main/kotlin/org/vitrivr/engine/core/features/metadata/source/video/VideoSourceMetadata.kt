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
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [VideoSourceMetadata] [Analyser], which derives metadata information from [Retr]
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
     * @param field The [Schema.Field] for which to create the [FileSourceMetadataExtractor].
     * @param input The input [Operator]
     * @param context The [IndexContext]
     * @param persisting Whether the resulting [FileSourceMetadataDescriptor]s should be persisted.
     *
     * @return [FileSourceMetadataExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): VideoSourceMetadataExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return VideoSourceMetadataExtractor(input, field, persisting)
    }

    /**
     * Generates and returns a new [FileSourceMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileSourceMetadataRetriever].
     * @param content The [List] of [ContentElement] to create [FileSourceMetadataRetriever] for. This is usually empty.
     * @param context The [QueryContext]
     *
     * @return [FileSourceMetadataRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): VideoSourceMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return VideoSourceMetadataRetriever(field, context)
    }

    /**
     * Generates and returns a new [FileSourceMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileSourceMetadataRetriever].
     * @param descriptors The [List] of [FileSourceMetadataDescriptor] to create [FileSourceMetadataRetriever] for. This is usually empty.
     * @param context The [QueryContext]
     *
     * @return [FileSourceMetadataRetriever]
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>,
        descriptors: Collection<VideoSourceMetadataDescriptor>,
        context: QueryContext
    ): Retriever<ContentElement<*>, VideoSourceMetadataDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return VideoSourceMetadataRetriever(field, context)
    }
}