package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.features.metadata.bool.SimpleBoolean
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * The [SimpleBooleanQueryDescriptor] is a  [StructDescriptor] which used to describe a boolean query for the [SimpleBoolean] [Analyser].
 *
 * Specifically, this [StructDescriptor] is never persisted and is simply used to express a query due to the current state of the architecture.
 * In a later step, [Analyser]s should not operate on the [Descriptor] level, but on a [Descriptor]'s [FieldSchema].
 */
@Deprecated("Since newRetrieverForQuery has been introduced")
data class SimpleBooleanQueryDescriptor(
    /**
     * The boolean [ComparisonOperator] this query builds on.
     */
    val comparator: ComparisonOperator,
    /**
     * The [StructSubField] addresses both, the [Schema.Field] and its [FieldSchema] to query on.
     * This is crucial, as the [StructSubField]'s [Type] specifies the type of the [value].
     */
    val subField: StructSubField,
    /**
     * The actual query value, of the [Type] specified by the [subField].
     */
    val value: Any
) : StructDescriptor {

    init {
        /* We are type agnostic to the subField and enforce this here */
        when(subField.fieldType){
            Type.STRING -> require(value is String){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.BOOLEAN -> require(value is Boolean){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.BYTE -> require(value is Byte){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.SHORT -> require(value is Short){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.INT -> require(value is Int){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.LONG -> require(value is Long){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.FLOAT -> require(value is Float){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.DOUBLE -> require(value is Double){"The addressed struct sub field is of type ${subField.fieldType}, which differs from value. This is a programmer's error!"}
            Type.DATETIME -> require(value is Date){"The addressed struct sub field is of tpye ${subField.fieldType}, which differs from value. This is a programmer's error!"}
        }
    }

    override val transient: Boolean = true
    override val id: DescriptorId = UUID.randomUUID()
    override val retrievableId = null // there should never be a retrievable directly associated with this descriptor.

    override fun schema(): List<FieldSchema> {
        TODO("Not yet implemented")
    }

    override fun values(): List<Pair<String, Any?>> {
        TODO("Not yet implemented")
    }

    fun toStringQuery(limit: Long): SimpleBooleanQuery<Value.String>{
        require(subField.fieldType == Type.STRING){"Cannot make a STRING query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.String(this.value as String), comparator, subField.fieldName, limit)
    }

    fun toBooleanQuery(limit: Long): SimpleBooleanQuery<Value.Boolean>{
        require(subField.fieldType == Type.BOOLEAN){"Cannot make a BOOLEAN query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Boolean(this.value as Boolean), comparator, subField.fieldName, limit)
    }

    fun toByteQuery(limit: Long): SimpleBooleanQuery<Value.Byte>{
        require(subField.fieldType == Type.BYTE){"Cannot make a BYTE query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Byte(this.value as Byte), comparator, subField.fieldName, limit)
    }

    fun toShortQuery(limit: Long): SimpleBooleanQuery<Value.Short>{
        require(subField.fieldType == Type.SHORT){"Cannot make a SHORT query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Short(this.value as Short), comparator, subField.fieldName, limit)
    }

    fun toIntQuery(limit: Long): SimpleBooleanQuery<Value.Int>{
        require(subField.fieldType == Type.INT){"Cannot make an INT query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Int(this.value as Int), comparator, subField.fieldName, limit)
    }

    fun toLongQuery(limit: Long): SimpleBooleanQuery<Value.Long>{
        require(subField.fieldType == Type.LONG){"Cannot make a LONG query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Long(this.value as Long), comparator, subField.fieldName, limit)
    }

    fun toFloatQuery(limit: Long): SimpleBooleanQuery<Value.Float>{
        require(subField.fieldType == Type.FLOAT){"Cannot make a FLOAT query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Float(this.value as Float), comparator, subField.fieldName, limit)
    }

    fun toDoubleQuery(limit: Long): SimpleBooleanQuery<Value.Double>{
        require(subField.fieldType == Type.DOUBLE){"Cannot make a DOUBLE query for a subfield of type ${subField.fieldType}"}
        return SimpleBooleanQuery(Value.Double(this.value as Double), comparator, subField.fieldName, limit)
    }
}
