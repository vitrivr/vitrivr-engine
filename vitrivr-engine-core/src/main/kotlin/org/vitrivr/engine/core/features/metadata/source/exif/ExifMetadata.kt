package org.vitrivr.engine.core.features.metadata.source.exif

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}

class ExifMetadata : Analyser<ContentElement<*>, MapStructDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = MapStructDescriptor::class

    override fun prototype() = MapStructDescriptor(UUID.randomUUID(), UUID.randomUUID(), emptyMap())

    override fun newExtractor(field: Schema.Field<ContentElement<*>, MapStructDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): ExifMetadataExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        logger.debug { "Creating new ExifMetadataExtractor for field '${field.fieldName}' with parameters $parameters." }
        return ExifMetadataExtractor(input, field, persisting)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, MapStructDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): ExifMetadataRetriever {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return ExifMetadataRetriever(field, context)
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
        descriptors: Collection<MapStructDescriptor>,
        context: QueryContext
    ): Retriever<ContentElement<*>, MapStructDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return ExifMetadataRetriever(field, context)
    }
}