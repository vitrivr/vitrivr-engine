package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.core.features.AbstractBatchedExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.operators.Operator

class FesExtractor<D:Descriptor, A:ExternalFesAnalyser<ContentElement<*>,D>>(
        input: Operator<Retrievable>,
        field: Schema.Field<ContentElement<*>, D>,
        persisting: Boolean = true
) : AbstractBatchedExtractor<ContentElement<*>, D>(input, field, persisting, 3) {
    override fun matches(retrievable: Retrievable): Boolean {
        val analyser = field.analyser as A
        return retrievable.filteredAttributes(ContentAttribute::class.java).any { contentItem ->
            analyser.contentClasses.any { contentClass ->
                contentClass.isInstance(contentItem.content)
            }
        }

    }

    override fun extract(retrievables: List<Retrievable>): List<List<D>> {
        val analyser = field.analyser as A

        val indexedContent = retrievables.flatMapIndexed { index, retrievable ->
            retrievable.filteredAttributes(ContentAttribute::class.java).mapNotNull { it.content }.filter{ contentItem -> analyser.contentClasses.any { contentClass -> contentClass.isInstance(contentItem) } }
                    .map { content -> IndexedValue(index, content) } // Keep track of each content's original index
        }

        val indices = indexedContent.map { it.index }
        val flattenedContent = indexedContent.map { it.value }

        val batchedResults = analyser.analyse(flattenedContent, field.parameters)

        if (batchedResults.size != flattenedContent.size) {
            throw IllegalStateException("The size of batched results does not match the input size.")
        }

        val resultsByOriginalIndices = batchedResults.mapIndexed { index, result ->
            IndexedValue(indices[index], result)
        }.groupBy { it.index }
                .mapValues { (_, value) -> value.map { it.value } } // Remove the IndexedValue wrapper

        // Reconstruct the original ragged structure using the indices to place results correctly
        return retrievables.indices.map { index ->
            resultsByOriginalIndices[index] ?: listOf()
        }
    }

}