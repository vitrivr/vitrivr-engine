package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.types.Type

/**
 * The [StructSubField] addresses a specific [FieldSchema] of a [StructDescriptor].
 */
data class StructSubField(
    /** The name of the field */
    val fieldName: String,
    /** The type of the field */
    val fieldType: Type
){
    companion object{
        internal fun FieldSchema.toStructField(): StructSubField {
            return StructSubField(this.name, this.type)
        }
    }
}
