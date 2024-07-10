package org.vitrivr.engine.database.pgvector

import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.Distance.*
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import java.sql.Date
import java.sql.PreparedStatement

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
    is Value.FloatVector -> this.setObject(index, PgVector(value.value))
    is Value.DoubleVector -> this.setObject(index, PgVector(value.value))
    is Value.IntVector -> this.setObject(index, PgVector(value.value))
    is Value.LongVector -> this.setObject(index, PgVector(value.value))
    is Value.BooleanVector -> this.setObject(index, PgBitVector(value.value))
    else -> throw IllegalArgumentException("Unsupported value type for vector value.")
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
 * Converts a [ComparisonOperator] to a SQL operator.
 *
 * @return SQL comparison operator.
 */
internal fun ComparisonOperator.toSql(): String = when (this){
    ComparisonOperator.EQ -> "="
    ComparisonOperator.NEQ -> "!="
    ComparisonOperator.LE -> "<"
    ComparisonOperator.GR -> ">"
    ComparisonOperator.LEQ -> ">="
    ComparisonOperator.GEQ -> "<="
    ComparisonOperator.LIKE -> "LIKE"
}