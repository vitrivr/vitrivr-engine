package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

/**
 * Appends stringified key-value pairs to a [Retrieved] based on the values of a [Field], if available
 */
class FieldLookup(override val input: Operator<Retrieved>, private val reader: DescriptorReader<*>) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()
        val ids = inputRetrieved.map { it.id }.toSet()
        if (ids.isEmpty()) {
            return@flow
        }

        /* Perform lookup. */
        val descriptors = reader.getAllBy(ids, "retrievableId").filter { it.retrievableId != null }.associateBy { it.retrievableId!! }
        inputRetrieved.forEach { retrieved ->
            val descriptor = descriptors[retrieved.id]
            if (descriptor != null) {
                retrieved.addAttribute(DescriptorAttribute(descriptor))
            }
            emit(retrieved)
        }
    }
}