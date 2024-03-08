package org.vitrivr.engine.core.features.metadata.source.file

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Implementation of the [FileSourceMetadata] [Analyser], which derives metadata information from file-based retrievables
 *
 * @author Ralph Gasser
 * @version 1.0.0
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
    override fun prototype(field: Schema.Field<*, *>) = FileSourceMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", 0, true)

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
    override fun newExtractor(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): FileSourceMetadataExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        logger.debug { "Creating new FileMetadataExtractor for field '${field.fieldName}' with parameters $parameters." }
        return FileSourceMetadataExtractor(input, field, persisting)
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
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): FileSourceMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return FileSourceMetadataRetriever(field, context)
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
        field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>,
        descriptors: Collection<FileSourceMetadataDescriptor>,
        context: QueryContext
    ): Retriever<ContentElement<*>, FileSourceMetadataDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return FileSourceMetadataRetriever(field, context)
    }
}