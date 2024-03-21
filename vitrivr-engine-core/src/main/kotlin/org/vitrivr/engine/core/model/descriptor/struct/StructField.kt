package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType

/**
 * The [StructField] addresses a specific [FieldSchema] of a [StructDescriptor].
 */
data class StructField(
    /** The name of the field */
    val fieldName: String,
    /** The type of the field */
    val fieldType: FieldType){
    companion object{
        internal fun FieldSchema.toStructField(): StructField {
            return StructField(this.name, this.type)
        }
    }
}
