package org.vitrivr.engine.query.operators.transform.lookup

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * [TransformerFactory] for the [MultiFieldLookup] operator.
 * This factory creates a [MultiFieldLookup] transformer that can look up multiple fields in a single operation.
 *
 * The fields to look up are specified as a comma-separated list in the "fields" property of the context.
 *
 * @version 1.0.0
 * @author henrikluemkemann
 */
class MultiFieldLookupFactory : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): MultiFieldLookup {
        require(context is QueryContext) { "Context must be a QueryContext" }
        
        // Get the comma separated list of fields from the context
        val fieldsString = context[name, "fields"] 
            ?: throw IllegalArgumentException("Expected 'fields' to be defined in properties")
        
        // Split the fields string into a list of field names
        val fieldNames = fieldsString.split(",").map { it.trim() }
        
        if (fieldNames.isEmpty()) {
            throw IllegalArgumentException("At least one field must be specified in 'fields' property")
        }
        
        // Create a map of field names to descriptor readers
        val readers = fieldNames.associateWith { fieldName ->
            val schemaField = context.schema[fieldName] ?: throw IllegalArgumentException("Field '$fieldName' not defined in schema")
            schemaField.getReader()
        }
        
        return MultiFieldLookup(input, readers, name)
    }
}