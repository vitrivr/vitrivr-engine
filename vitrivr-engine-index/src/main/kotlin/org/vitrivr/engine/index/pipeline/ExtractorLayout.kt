package org.vitrivr.engine.index.pipeline

import org.vitrivr.engine.core.model.metamodel.FieldName
import org.vitrivr.engine.core.model.metamodel.Schema

data class ExtractorLayout(val schema: Schema, val layout: List<List<ExtractorOption>>) {

    init {
        val flat = layout.flatten().map { it.name }
        assert(flat.isNotEmpty()) {"Layout cannot be empty"}
        assert(flat.size == flat.toSet().size) {"FieldNames must be unique"}
        flat.forEach {
            if (schema.getField(it) == null) {
                throw AssertionError("FieldName '$it' not found")
            }
        }
    }

    data class ExtractorOption(val name: FieldName, val persisting: Boolean = true)

}
