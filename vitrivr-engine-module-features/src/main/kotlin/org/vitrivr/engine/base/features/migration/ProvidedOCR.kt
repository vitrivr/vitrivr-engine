package org.vitrivr.engine.base.features.migration

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class ProvidedOCR : Analyser<ContentElement<*>, StringDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = StringDescriptor::class
    override fun prototype(field: Schema.Field<*, *>) = StringDescriptor(id = UUID.randomUUID(), retrievableId = UUID.randomUUID(), value = Value.String(""))
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, StringDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, StringDescriptor>, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, StringDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, StringDescriptor> {
        TODO("Not yet implemented")
    }
}