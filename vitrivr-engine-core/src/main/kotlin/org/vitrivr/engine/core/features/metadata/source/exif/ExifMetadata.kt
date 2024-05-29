package org.vitrivr.engine.core.features.metadata.source.exif

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class ExifMetadata : Analyser<ContentElement<*>, MapStructDescriptor> {

    private val logger: KLogger = KotlinLogging.logger {}

    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = MapStructDescriptor::class
    override fun prototype(field: Schema.Field<*, *>) = MapStructDescriptor.prototype(field.parameters)
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, MapStructDescriptor> {
        return ExifMetadataExtractor(input, null)
    }

    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, MapStructDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return ExifMetadataExtractor(input, field)
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, MapStructDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, MapStructDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        require(query is SimpleBooleanQuery<*>) { "Query is not a Query." }
        return ExifMetadataRetriever(field, query, context)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, MapStructDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): ExifMetadataRetriever {
        throw UnsupportedOperationException("ExifMetadata does not support the creation of a Retriever instance from content.")
    }
}