package org.vitrivr.engine.query.operators.transform.lookup

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory


/**
 * [TransformerFactory] for [ObjectFieldLookup].
 *
 * @version 1.0.0
 * @author Ralph Gasser
 */
class ObjectFieldLookupFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, schema: Schema, properties: Map<String, String>): ObjectFieldLookup {
        val field = properties["field"] ?: throw IllegalArgumentException("Expected 'field' to be defined in properties")
        val predicates = properties["predicates"]?.split(",")?.toSet() ?: emptySet()
        val reader = (schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")).getReader()
        return ObjectFieldLookup(input, reader, predicates, name)
    }
}