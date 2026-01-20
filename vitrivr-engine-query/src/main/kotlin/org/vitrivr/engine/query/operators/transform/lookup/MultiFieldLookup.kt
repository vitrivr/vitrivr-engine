package org.vitrivr.engine.query.operators.transform.lookup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * Appends [Descriptor]s to a [Retrieved] based on the values of multiple [Schema.Field]s, if available.
 * This transformer allows looking up multiple fields in a single operation, which is more efficient
 * than chaining multiple [FieldLookup] transformers.
 *
 * @param input The input operator that provides retrievables to be enriched.
 * @param readers A map of field names to descriptor readers for those fields.
 * @param name The name of this transformer.
 *
 * @version 1.0.0
 * @author henrikluemkemann
 */
class MultiFieldLookup(
    override val input: Operator<out Retrievable>,
    private val readers: Map<String, DescriptorReader<*>>,
    override val name: String
) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        //Parse input IDs.
        val inputRetrieved = input.toFlow(scope).toList()
        
        // If there are no inputs or no readers, just pass through the input
        if (inputRetrieved.isEmpty() || readers.isEmpty()) {
            inputRetrieved.forEach { emit(it) }
            return@flow
        }
        
        // Fetch entries for the provided IDs from all readers.
        val ids = inputRetrieved.map { it.id }.toSet()
        
        // Create a map of retrievable IDs to all descriptors from all readers
        val allDescriptors = mutableMapOf<RetrievableId, MutableList<Descriptor<*>>>()
        
        // Fetch descriptors from each reader and group them by retrievable ID
        for ((fieldName, reader) in readers) {
            val descriptors = if (ids.isEmpty()) {
                emptyMap()
            } else {
                reader.getAllForRetrievable(ids).groupBy { it.retrievableId!! }
            }
            
            // Add descriptors to the combined map
            for ((id, descriptorList) in descriptors) {
                allDescriptors.getOrPut(id) { mutableListOf() }.addAll(descriptorList)
            }
        }
        
        //Emit retrievable with added attributes.
        inputRetrieved.forEach { retrieved ->
            val descriptors = allDescriptors[retrieved.id]
            if (descriptors != null && descriptors.isNotEmpty()) {
                // Convert descriptors to property map
                val propertyMap = mutableMapOf<String, String>()
                
                // Process each descriptor and extract properties
                for (descriptor in descriptors) {
                    // Get the field name from the descriptor
                    val fieldName = descriptor.field?.fieldName ?: continue
                    
                    // Get the values from the descriptor and convert them to strings
                    val values = descriptor.values()
                    for ((key, value) in values) {
                        propertyMap["${fieldName}_${key}"] = value?.toString() ?: "null"
                    }
                }
                
                // Create a PropertyAttribute from the property map
                val propertyAttribute = PropertyAttribute(propertyMap)
                
                // Add the descriptors and the PropertyAttribute to the retrievable
                val updatedAttributes = retrieved.attributes + propertyAttribute
                
                emit(retrieved.copy(
                    descriptors = retrieved.descriptors + descriptors,
                    attributes = updatedAttributes
                ))
            } else {
                emit(retrieved)
            }
        }
    }
}