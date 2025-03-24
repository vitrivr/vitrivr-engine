package org.vitrivr.engine.query.operators.transform.lookup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import javax.management.Descriptor

/**
 * Appends [Descriptor]s to a [Retrieved] based on the values of a [Schema.Field], if available.
 *
 * @version 1.2.0
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class FieldLookup(
    override val input: Operator<out Retrievable>,
    private val reader: DescriptorReader<*>,
    override val name: String
) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()

        /* Fetch entries for the provided IDs. */
        val ids = inputRetrieved.map { it.id }.toSet()
        val descriptors = if (ids.isEmpty()) {
            emptyMap()
        } else {
            this@FieldLookup.reader.getAllForRetrievable(ids).groupBy { it.retrievableId!! }
        }

        /* Emit retrievable with added attribute. */
        inputRetrieved.forEach { retrieved ->
            val descriptor = descriptors[retrieved.id]
            if (descriptor != null) {
                emit(retrieved.copy(descriptors = retrieved.descriptors + descriptor))
            } else {
                emit(retrieved)
            }
        }
    }
}
