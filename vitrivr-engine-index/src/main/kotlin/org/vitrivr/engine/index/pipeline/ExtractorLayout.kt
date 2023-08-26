package org.vitrivr.engine.index.pipeline

import org.vitrivr.engine.core.model.metamodel.FieldName
import org.vitrivr.engine.core.model.metamodel.Schema

data class ExtractorLayout(val schema: Schema, val layout: List<List<FieldName>>) {

    init {
        val flat = layout.flatten()
        assert(flat.isNotEmpty()) {"Layout cannot be empty"}
        assert(flat.size == flat.toSet().size) {"FieldNames must be unique"}
        flat.forEach {
            if (schema.getField(it) == null) {
                throw AssertionError("FieldName '$it' not found")
            }
        }
    }

}
