package org.vitrivr.engine.database.pgvector.exposed.types

import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.PGobject

/**
 * A [ColumnType] for ts_vector values.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TsVectorColumnType: ColumnType<String>(nullable = true) {
    companion object {
        private const val TS_VECTOR_SQL_TYPE = "tsvector"
    }

    override fun sqlType(): String = TS_VECTOR_SQL_TYPE

    override fun valueFromDB(value: Any): String? {
        return value as String
    }

    override fun valueToDB(value: String?): Any? {
        return PGobject().apply {
            type = TS_VECTOR_SQL_TYPE
            this.value = value
        }.value
    }


    override fun notNullValueToDB(value: String): Any {
        return PGobject().apply {
            type = TS_VECTOR_SQL_TYPE
            this.value = value
        }
    }

    override fun nonNullValueToString(value: String): String {
        return "'$value'"
    }
}