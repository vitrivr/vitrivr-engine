package org.vitrivr.engine.query.operators.transform.lookup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import javax.management.Descriptor

/**
 * Appends [Descriptor] to a [Retrieved] based on the values of a [Schema.Field], if available.
 *
 * @version 1.1.1
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class FieldLookup(
    override val input: Operator<out Retrievable>,
    private val reader: DescriptorReader<*>,
    val keys: List<String>
) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
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
                retrieved.addDescriptor(descriptor)
                /* Somewhat experimental. Goal: Attach information in a meaningful manner, such that it can be serialised */
                val descriptorValuesAsMap = descriptor.values().toMap()
                retrieved.addAttribute(PropertyAttribute(keys.map{
                    it to "${descriptorValuesAsMap[it]}"
                }.toMap()))
            }
            emit(retrieved)
        }
    }
}
