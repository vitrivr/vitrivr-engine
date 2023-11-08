package org.vitrivr.engine.core.features.metadata.temporal

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.metadata.file.FileMetadata
import org.vitrivr.engine.core.features.metadata.file.FileMetadataExtractor
import org.vitrivr.engine.core.features.metadata.file.FileMetadataRetriever
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * Implementation of the [TemporalMetadata] [Analyser], which derives metadata information [TemporalContent].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

private val logger: KLogger = KotlinLogging.logger {}

class TemporalMetadata : Analyser<ContentElement<*>, TemporalMetadataDescriptor> {
    override val contentClass = ContentElement::class
    override val descriptorClass = TemporalMetadataDescriptor::class

    /**
     * Generates a prototypical [TemporalMetadataDescriptor] for this [FileMetadata].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = TemporalMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), 0, 0, true)

    /**
     * Generates and returns a new [FileMetadataExtractor] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileMetadataExtractor].
     * @param input The input [Operator]
     * @param context The [IndexContext]
     * @param persisting Whether the resulting [TemporalMetadataDescriptor]s should be persisted.
     *
     * @return [TemporalMetadataExtractor]
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): TemporalMetadataExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        logger.debug { "Creating new TemporalMetadataExtractor for field '${field.fieldName}' with parameters $parameters." }
        return TemporalMetadataExtractor(input, field, persisting)
    }

    /**
     * Generates and returns a new [FileMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileMetadataRetriever].
     * @param content The [List] of [ContentElement] to create [FileMetadataRetriever] for. This is usually empty.
     * @param context The [QueryContext]
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>,
        content: Collection<ContentElement<*>>,
        context: QueryContext
    ): TemporalMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return TemporalMetadataRetriever(field, context)
    }

    /**
     * Generates and returns a new [FileMetadataRetriever] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [FileMetadataRetriever].
     * @param descriptors The [List] of [TemporalMetadataDescriptor] to create [FileMetadataRetriever] for. This is usually empty.
     * @param context The [QueryContext]
     * @return [TemporalMetadataRetriever]
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>,
        descriptors: Collection<TemporalMetadataDescriptor>,
        context: QueryContext
    ): TemporalMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return TemporalMetadataRetriever(field, context)
    }
}