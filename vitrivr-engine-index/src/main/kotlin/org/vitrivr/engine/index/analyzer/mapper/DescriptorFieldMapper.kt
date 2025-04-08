package org.vitrivr.engine.index.analyzer.mapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import kotlin.reflect.KClass

abstract class DescriptorFieldMapper<D : Descriptor<*>> : Analyser<ContentElement<*>, D> {

    override val contentClasses: Set<KClass<out ContentElement<*>>> = emptySet() //no content is processed

    companion object {
        const val LENGTH_PARAMETER_NAME = "length"
        const val AUTHORNAME_PARAMETER_NAME = "authorName"
    }


    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, D>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, D> {
        val authorName = field.parameters[AUTHORNAME_PARAMETER_NAME]
            ?: throw IllegalArgumentException("'$AUTHORNAME_PARAMETER_NAME' is not defined")
        return Mapper(input, field, authorName)
    }

    protected abstract fun cast(descriptor: Descriptor<*>, field: Schema.Field<ContentElement<*>, D>) : D

    inner class Mapper(
        override val input: Operator<out Retrievable>,
        override val field: Schema.Field<ContentElement<*>, D>,
        private val authorName: String
    ) : Extractor<ContentElement<*>, D> {
        override val analyser = this@DescriptorFieldMapper
        override val name = field.fieldName
        override val persisting = true

        override fun toFlow(scope: CoroutineScope) = this.input.toFlow(scope).onEach { retrievable ->

            val ids = retrievable.filteredAttribute(DescriptorAuthorAttribute::class.java)?.getDescriptorIds(authorName) ?: return@onEach
            val descriptors = retrievable.descriptors.filter { it.id in ids }

            if (descriptors.isEmpty()) {
                return@onEach
            }

            val typeChecked = descriptors.map {
                cast(it, field)
            }

            /* descriptors.forEach {
                retrievable.removeDescriptor(it)
            }

            val authorAttribute = DescriptorAuthorAttribute()

            typeChecked.forEach {
                retrievable.addDescriptor(it)
                authorAttribute.add(it, this.name)
            }

            retrievable.addAttribute(authorAttribute) */

            /** TODO: @lucaro. */
        }

    }

    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, D> {
        throw UnsupportedOperationException("DescriptorPersister required backing field")
    }

    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, D>,
        query: Query,
        context: QueryContext
    ): Retriever<ContentElement<*>, D> {
        throw UnsupportedOperationException("DescriptorPersister does not support retrieval")
    }

    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, D>,
        content: Collection<ContentElement<*>>,
        context: QueryContext
    ): Retriever<ContentElement<*>, D> {
        throw UnsupportedOperationException("DescriptorPersister does not support retrieval")
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, D>,
        descriptors: Collection<D>,
        context: QueryContext
    ): Retriever<ContentElement<*>, D> {
        throw UnsupportedOperationException("DescriptorPersister does not support retrieval")
    }
}