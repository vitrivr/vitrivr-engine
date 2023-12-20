package org.vitrivr.engine.base.features.fulltext

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Analyser for the Optical Chracter Reckognition (OCR).
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class OCR : Analyser<ContentElement<*>, StringDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = StringDescriptor::class
    override fun prototype(): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", true)

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, StringDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return this.newRetrieverForDescriptors(field, this.analyse(content), context)
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, StringDescriptor>, descriptors: Collection<StringDescriptor>, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return FulltextRetriever(field, descriptors.first(), context)
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, StringDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, StringDescriptor> {
        throw UnsupportedOperationException("OCR does not allow for extraction.")
    }

    fun analyse(content: Collection<ContentElement<*>>): List<StringDescriptor> = content.map {
        when (it) {
            is TextContent -> StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), it.content, true)
            else -> throw UnsupportedOperationException("OCR does not allow for data extraction from non-textual content.")
        }
    }
}