package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

/**
 * Appends stringified key-value pairs to a [Retrieved] based on the values of a [Field], if available
 */
class FieldLookup(
    override val input: Operator<Retrieved>,
    private val reader: DescriptorReader<*>,
    private val keys: Collection<String>

) : Transformer<Retrieved, Retrieved.RetrievedWithProperties> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved.RetrievedWithProperties> = flow {

        val inputRetrieved = input.toFlow(scope).toList()

        val ids = inputRetrieved.map { it.id }.toSet()

        if (ids.isEmpty()) {
            return@flow
        }

        val descriptors = reader.getAllBy(ids, "retrievableId").filter { it.retrievableId != null }.associateBy { it.retrievableId!! }

        inputRetrieved.forEach {retrieved ->

            val descriptor = descriptors[retrieved.id]

            val withProperties = Retrieved.PlusProperties(retrieved)

            if (descriptor != null) {
                val values = descriptor.values().toMap()
                keys.forEach { key ->
                    (withProperties.properties as MutableMap)[key] = values[key].toString()
                }
            }

            emit(withProperties)

        }

    }

}