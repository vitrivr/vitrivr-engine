package org.vitrivr.engine.core.features.metadata.file

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.FileMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [FileMetadata] [Analyser], which derives metadata information from file-based retrievables
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileMetadata : Analyser<ContentElement<*>, FileMetadataDescriptor> {
    override val contentClass = ContentElement::class
    override val descriptorClass = FileMetadataDescriptor::class

    /**
     * Generates a prototypical [FileMetadataDescriptor] for this [FileMetadata].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FileMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", 0, true)

    /**
     * Generates and returns a new [FileMetadataExtractor] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileMetadataExtractor].
     * @param input The input [Operator]
     * @param context The [IndexContext]
     * @param persisting Whether the resulting [FileMetadataDescriptor]s should be persisted.
     *
     * @return [FileMetadataExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, FileMetadataDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean): FileMetadataExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return FileMetadataExtractor(field, input, persisting)
    }

    /**
     * Generates and returns a new [FileMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileMetadataRetriever].
     * @param content The [List] of [ContentElement] to create [FileMetadataRetriever] for. This is usually empty.
     * @param context The [QueryContext]
     *
     * @return [FileMetadataRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FileMetadataDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): FileMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return FileMetadataRetriever(field, context)
    }

    /**
     * Generates and returns a new [FileMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileMetadataRetriever].
     * @param descriptors The [List] of [FileMetadataDescriptor] to create [FileMetadataRetriever] for. This is usually empty.
     * @param context The [QueryContext]
     *
     * @return [FileMetadataRetriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, FileMetadataDescriptor>, descriptors: Collection<FileMetadataDescriptor>, context: QueryContext): Retriever<ContentElement<*>, FileMetadataDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return FileMetadataRetriever(field, context)
    }
}