package org.vitrivr.engine.core.features.metadata.bool

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.SimpleBooleanQueryDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**
 * A specific [Analyser] for [StructDescriptor] backed [Schema.Field]s.
 *
 * During extraction, this [Analyser]'s behaviour is not defined,
 * as the individual fields should implement their respective extractors.
 *
 * During retrieval, this [Analyser] performs simple boolean retrieval on those [Schema.Field]s,
 * therefore on the actual descriptor.
 */
class SimpleBoolean : Analyser<ContentElement<*>, StructDescriptor> {
    private val logger: KLogger = KotlinLogging.logger {}

    companion object{
        const val DESCRIPTOR_PARAMETER_NAME = "descriptor"
    }

    override val descriptorClass = StructDescriptor::class
    override val contentClasses = setOf(ContentElement::class)


    override fun prototype(field: Schema.Field<*, *>): StructDescriptor {
        /* We are configuration agnostic and hence have to reflect on the actual descriptor */
        val descriptorName = field.parameters[DESCRIPTOR_PARAMETER_NAME] ?: throw IllegalArgumentException("Must have a descriptor specified!")
        val analyser = loadServiceForName<Analyser<*,StructDescriptor>>(descriptorName) ?: throw IllegalArgumentException("Failed to find a factory implementation for '$descriptorName'")
        return analyser.prototype(field)
    }

    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, StructDescriptor>,
        content: Collection<ContentElement<*>>,
        context: QueryContext
    ): Retriever<ContentElement<*>, StructDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, StructDescriptor>,
        descriptors: Collection<StructDescriptor>,
        context: QueryContext
    ): Retriever<ContentElement<*>, StructDescriptor> {
        /* First check if the correct descriptor is passed */
        require(descriptors.first() is SimpleBooleanQueryDescriptor){"SimpleBoolean requires SimpleBooleanQueryDescriptor and is not compatible with other descriptors for retrieval"}
        return SimpleBooleanRetriever(field,
            descriptors.first() as SimpleBooleanQueryDescriptor, context)
    }

    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, StructDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ContentElement<*>, StructDescriptor> {
        throw UnsupportedOperationException("The ${this.javaClass.simpleName} does not have an extractor. This is a programmer's error!")
    }
}
