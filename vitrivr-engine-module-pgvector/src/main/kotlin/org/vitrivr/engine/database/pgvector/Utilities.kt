package org.vitrivr.engine.database.pgvector

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.SortOrder
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.descriptor.PgDescriptorInitializer
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import org.vitrivr.engine.database.pgvector.tables.AbstractDescriptorTable
import org.vitrivr.engine.database.pgvector.tables.scalar.*
import org.vitrivr.engine.database.pgvector.tables.StructDescriptorTable
import org.vitrivr.engine.database.pgvector.tables.vector.FloatVectorDescriptorTable
import java.sql.Date
import java.sql.PreparedStatement


/**
 * Converts a [Schema.Field] to a [AbstractDescriptorTable] that supports the [Descriptor] type.
 *
 * @return [AbstractDescriptorTable]
 * @throws [IllegalArgumentException] if the [Descriptor] type is not supported.
 */
internal fun <D: Descriptor<*>> Schema.Field<*, D>.toTable() = when (this.analyser.prototype(this)) {
    is BooleanDescriptor -> BooleanDescriptorTable(this as Schema.Field<*, BooleanDescriptor>)
    is ByteDescriptor -> ByteDescriptorTable(this as Schema.Field<*, ByteDescriptor>)
    is DoubleDescriptor -> DoubleDescriptorTable(this as Schema.Field<*, DoubleDescriptor>)
    is FloatDescriptor -> FloatDescriptorTable(this as Schema.Field<*, FloatDescriptor>)
    is IntDescriptor -> IntDescriptorTable(this as Schema.Field<*, IntDescriptor>)
    is LongDescriptor -> LongDescriptorTable(this as Schema.Field<*, LongDescriptor>)
    is ShortDescriptor -> ShortDescriptorTable(this as Schema.Field<*, ShortDescriptor>)
    is StringDescriptor -> StringDescriptorTable(this as Schema.Field<*, StringDescriptor>)
    is TextDescriptor -> TextDescriptorTable(this as Schema.Field<*, TextDescriptor>)
    is FloatVectorDescriptor -> FloatVectorDescriptorTable(this as Schema.Field<*, FloatVectorDescriptor>)
    is StructDescriptor<*> -> StructDescriptorTable(this as Schema.Field<*, StructDescriptor<*>>)
    else -> throw IllegalArgumentException("Unsupported descriptor type: ${this.analyser.prototype(this)}")
} as AbstractDescriptorTable<D>


/**
 * Converts [SortOrder] between vitrivr and Exposed.
 */
internal fun SortOrder.toSql() = when (this) {
    SortOrder.ASC -> org.jetbrains.exposed.sql.SortOrder.ASC
    SortOrder.DESC -> org.jetbrains.exposed.sql.SortOrder.DESC
}
/**
 * Sets a value of [Value] type in a [PreparedStatement].
 *
 * @param index The index to set.
 * @param value The [Value] to set.
 */
internal fun PreparedStatement.setValue(index: Int, value: Value<*>) = when (value) {
    is Value.Boolean -> this.setBoolean(index, value.value)
    is Value.Byte -> this.setByte(index, value.value)
    is Value.DateTime -> this.setDate(index, Date(value.value.toInstant().toEpochMilli()))
    is Value.Double -> this.setDouble(index, value.value)
    is Value.Float -> this.setFloat(index, value.value)
    is Value.Int -> this.setInt(index, value.value)
    is Value.Long -> this.setLong(index, value.value)
    is Value.Short -> this.setShort(index, value.value)
    is Value.String -> this.setString(index, value.value)
    is Value.Text -> this.setString(index, value.value)
    is Value.FloatVector -> this.setObject(index, PgVector(value.value))
    is Value.DoubleVector -> this.setObject(index, PgVector(value.value))
    is Value.IntVector -> this.setObject(index, PgVector(value.value))
    is Value.LongVector -> this.setObject(index, PgVector(value.value))
    is Value.BooleanVector -> this.setObject(index, PgBitVector(value.value))
    else -> throw IllegalArgumentException("Unsupported value type for vector value.")
}

/**
 * Closes the [PgDescriptorInitializer].
 */
internal fun Distance.toIndexName() = when (this) {
    Distance.MANHATTAN -> "vector_l1_ops"
    Distance.EUCLIDEAN -> "vector_l2_ops"
    Distance.IP -> "vector_ip_ops"
    Distance.COSINE -> "vector_cosine_ops"
    Distance.HAMMING -> "bit_hamming_ops"
    Distance.JACCARD -> "bit_jaccard_ops"
}