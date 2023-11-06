package org.vitrivr.engine.base.features.OCRSearch

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*
import kotlin.reflect.KClass

class OCRSearch : Analyser<ImageContent,StringDescriptor> {
    override val contentClass = ImageContent::class
    override val descriptorClass = StringDescriptor::class
    override fun prototype(): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", true)
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, StringDescriptor>, content: Collection<ImageContent>, context: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return this.newRetrieverForDescriptors(field, this.analyse(content), context)
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, StringDescriptor>, descriptors: Collection<StringDescriptor>, context: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return OCRSearchRetriever(field, descriptors.first(), context)
    }

    override fun newExtractor(
        field: Schema.Field<ImageContent, StringDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ImageContent, StringDescriptor> {
        TODO("Not yet implemented")
    }

    fun analyse(content: Collection<ImageContent>): List<StringDescriptor> {
        TODO("Not yet implemented")
    }

}