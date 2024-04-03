package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

/**
 * Appends [DescriptorAttribute] to a [Retrieved] based on the values of a [Schema.Field], if available.
 *
 * @version 1.1.0
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class FieldLookup(override val input: Operator<Retrieved>, private val reader: DescriptorReader<*>) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()

        /* Fetch entries for the provided IDs. */
        val ids = inputRetrieved.map { it.id }.toSet()
        val descriptors = if (ids.isEmpty()) {
            emptyMap()
        } else {
            this@FieldLookup.reader.getAllBy(ids, "retrievableId").filter { it.retrievableId != null }.associateBy { it.retrievableId!! }
        }

        /* Emit retrievable with added attribute. */
        inputRetrieved.forEach { retrieved ->
            val descriptor = descriptors[retrieved.id]
            if (descriptor != null) {
                retrieved.addAttribute(DescriptorAttribute(descriptor))
            }
            emit(retrieved)
        }
    }
}