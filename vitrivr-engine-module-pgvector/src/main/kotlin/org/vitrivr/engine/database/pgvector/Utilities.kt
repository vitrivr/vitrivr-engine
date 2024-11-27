package org.vitrivr.engine.database.pgvector

import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.Distance.*
import org.vitrivr.engine.core.model.query.bool.BooleanPredicate
import org.vitrivr.engine.core.model.query.bool.Comparison
import org.vitrivr.engine.core.model.query.bool.Logical
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import org.vitrivr.engine.database.pgvector.descriptor.scalar.ScalarDescriptorReader
import org.vitrivr.engine.database.pgvector.descriptor.struct.StructDescriptorReader
import java.sql.Date
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.SQLType

/**
 * Converts a [Type] to a [SQLType].
 */
internal fun Type.toSql(): JDBCType = when (this) {
    Type.Boolean -> JDBCType.BOOLEAN
    Type.Byte -> JDBCType.TINYINT
    Type.Short -> JDBCType.SMALLINT
    Type.Int -> JDBCType.INTEGER
    Type.Long -> JDBCType.BIGINT
    Type.Float -> JDBCType.REAL
    Type.Double -> JDBCType.DOUBLE
    Type.Datetime -> JDBCType.DATE
    Type.String -> JDBCType.VARCHAR
    Type.Text -> JDBCType.CLOB
    Type.UUID -> JDBCType.OTHER
    is Type.BooleanVector -> JDBCType.ARRAY
    is Type.DoubleVector -> JDBCType.ARRAY
    is Type.FloatVector -> JDBCType.ARRAY
    is Type.IntVector -> JDBCType.ARRAY
    is Type.LongVector -> JDBCType.ARRAY
}

/**
 * Converts a [Distance] to a pgVector distance operator.
 */
fun Distance.toSql() = when(this) {
    MANHATTAN -> "<+>"
    EUCLIDEAN -> "<->"
    COSINE -> "<=>"
    HAMMING -> "<~>"
    JACCARD -> "<%>"
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
 * Sets a list of [Value]s in a [PreparedStatement] for a [Comparison].
 */
internal fun PreparedStatement.setValueForComparison(index: Int, comparison: Comparison<*>) = when (comparison) {
    is Comparison.Equals<*> -> this.setValue(index, comparison.value)
    is Comparison.NotEquals<*> -> this.setValue(index, comparison.value)
    is Comparison.Greater<*> -> this.setValue(index, comparison.value)
    is Comparison.GreaterEquals<*> -> this.setValue(index, comparison.value)
    is Comparison.Less<*> -> this.setValue(index, comparison.value)
    is Comparison.LessEquals<*> -> this.setValue(index, comparison.value)
    is Comparison.Like<*> -> this.setValue(index, comparison.value)
    is Comparison.In<*> -> this.setArray(index, this.connection.createArrayOf(comparison.values.first().type.toSql().toString(), comparison.values.map { it.value }.toTypedArray()))
}

/**
 * Converts a [BooleanPredicate] to an SQL WHERE-clause
 */
internal fun BooleanPredicate.toWhere(): String = when (this) {
    is Comparison<*> -> this.toTerm()
    is Logical.And -> this.predicates.joinToString(" AND ", "(", ")") { it.toWhere() }
    is Logical.Or -> this.predicates.joinToString(" OR ", "(", ")") { it.toWhere() }
    is Logical.Not -> "NOT (${this.predicate.toWhere()})"
}

/**
 * Converts a [Comparison] to an SQL comparison term.
 */
internal fun Comparison<*>.toTerm(): String {
    val reader = this.field.getReader()
    var (tableName, fieldName) = if (reader is ScalarDescriptorReader) {
        reader.tableName to ScalarDescriptor.VALUE_ATTRIBUTE_NAME
    } else if (reader is StructDescriptorReader) {
        reader.tableName to (this.attributeName ?: throw IllegalArgumentException("Attribute name must be provided for struct descriptors."))
    } else {
        throw IllegalArgumentException("Unsupported reader type.")
    }

    return when (this) {
        is Comparison.Equals<*> -> "$tableName.$fieldName = ?"
        is Comparison.NotEquals<*> -> "$tableName.$fieldName != ?"
        is Comparison.Greater<*> -> "$tableName.$fieldName > ?"
        is Comparison.GreaterEquals<*> -> "$tableName.$fieldName >= ?"
        is Comparison.Less<*> -> "$tableName.$fieldName < ?"
        is Comparison.LessEquals<*> -> "$tableName.$fieldName <= ?"
        is Comparison.Like<*> -> "$tableName.$fieldName LIKE ?"
        is Comparison.In<*> -> "$tableName.$fieldName = ANY(?)"
    }
}
