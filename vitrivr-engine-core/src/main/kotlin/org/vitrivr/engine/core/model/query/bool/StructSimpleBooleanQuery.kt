package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator

/**
 * A [StructSimpleBooleanQuery] on a [StructDescriptor] of type [T], querying for the value of [V].
 *
 * Boolean queries often times use comparative operators such as less than, less or equal than.
 * In the context of [StructDescriptor]s, these comparisons are based on a field name.
 */
data class StructSimpleBooleanQuery<T : StructDescriptor,V>(
    val fieldName: String,
    val fieldAndValueType: FieldType,
    val value: V,
    override val descriptor: T,
    override val comparison: ComparisonOperator,
) : SimpleBooleanQuery<T>{
    init{
        when(fieldAndValueType){
            FieldType.STRING -> require(value is String){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.BOOLEAN -> require(value is Boolean){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.BYTE -> require(value is Byte){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.SHORT -> require(value is Short){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.INT -> require(value is Int){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.LONG -> require(value is Long){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.FLOAT -> require(value is Float){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
            FieldType.DOUBLE -> require(value is Double){"The value has to match the field type ($fieldAndValueType), but was ${value!!::class}"}
        }
    }
}
