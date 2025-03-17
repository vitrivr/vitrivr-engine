package org.vitrivr.engine.database.pgvector.exposed.types

import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.PGobject
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector

/**
 * A [ColumnType] for [Short] values.
 *
 * This class is used to define the column type for short values in the database.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorColumnType(val dimension: Int): ColumnType<FloatArray>() {
    override fun sqlType(): String = "vector($dimension)"
    override fun valueFromDB(value: Any): FloatArray = when (value) {
        is PgVector -> value.vec ?: error("Unexpected null value for vector.")
        is PGobject -> value.value?.let { PgVector(it) }?.vec  ?: error("Unexpected null value for vector.")
        else -> error("Unexpected value of type vector: $value of ${value::class.qualifiedName}")
    }
    override fun notNullValueToDB(value: FloatArray) = PgVector(value)
}