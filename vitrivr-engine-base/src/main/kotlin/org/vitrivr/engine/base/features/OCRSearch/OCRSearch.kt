package org.vitrivr.engine.base.features.OCRSearch

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*
import kotlin.reflect.KClass

class OCRSearch : Analyser<ImageContent,StringDescriptor> {

    override val analyserName: String = "OCRSearch"
    override val contentClass = ImageContent::class
    override val descriptorClass = StringDescriptor::class
    override fun prototype(): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", true)
    override fun newRetriever(field: Schema.Field<ImageContent, StringDescriptor>, content: Collection<ImageContent>, queryContext: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content), queryContext)
    }

    override fun newRetriever(field: Schema.Field<ImageContent, StringDescriptor>, descriptors: DescriptorList<StringDescriptor>, queryContext: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { }
        return OCRSearchRetriever(field, descriptors.first(), queryContext)
    }

    override fun newExtractor(field: Schema.Field<ImageContent, StringDescriptor>, input: Operator<Ingested>, persisting: Boolean): Extractor<ImageContent, StringDescriptor> {
        TODO("Not yet implemented")
    }

    override fun analyse(content: Collection<ImageContent>): DescriptorList<StringDescriptor> {
        TODO("Not yet implemented")
    }
}