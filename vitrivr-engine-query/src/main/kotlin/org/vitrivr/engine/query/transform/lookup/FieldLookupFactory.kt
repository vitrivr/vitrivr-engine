package org.vitrivr.engine.query.transform.lookup

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.TransformerFactory

class FieldLookupFactory() : TransformerFactory {
    override fun newTransformer(input: Operator<Retrieved>, schema: Schema, properties: Map<String, String>): FieldLookup {
        val keys = properties["keys"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        val field = properties["field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val reader = (schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")).getReader()
        return FieldLookup(input, reader)
    }
}