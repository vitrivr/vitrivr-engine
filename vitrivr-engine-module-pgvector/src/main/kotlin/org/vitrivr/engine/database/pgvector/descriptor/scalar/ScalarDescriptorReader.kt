package org.vitrivr.engine.database.pgvector.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.Comparison
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextPredicate
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorReader
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

/**
 * A [AbstractDescriptorReader] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*, *>>, connection: PgVectorConnection) : AbstractDescriptorReader<ScalarDescriptor<*, *>>(field, connection) {

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<ScalarDescriptor<*, *>> = sequence {
        when (val predicate = query.predicate) {
            is SimpleFulltextPredicate -> prepareFulltext(predicate)
            is Comparison<*> -> prepareComparison(predicate)
            else -> throw IllegalArgumentException("Query of type ${query::class} is not supported by ScalarDescriptorReader.")
        }.use { stmt ->
            stmt.executeQuery().use { result ->
                while (result.next()) {
                    yield(rowToDescriptor(result))
                }
            }
        }
    }

    /**
     * Converts the provided [ResultSet] to a [VectorDescriptor].
     *
     * @param result The [ResultSet] to convert.
     * @return The resulting [VectorDescriptor].
     */
    override fun rowToDescriptor(result: ResultSet): ScalarDescriptor<*, *> {
        val descriptorId = result.getObject(DESCRIPTOR_ID_COLUMN_NAME, UUID::class.java)
        val retrievableId = result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java)
        return when (this.prototype) {
            is BooleanDescriptor -> BooleanDescriptor(descriptorId, retrievableId, Value.Boolean(result.getBoolean(VALUE_ATTRIBUTE_NAME)))
            is ByteDescriptor -> ByteDescriptor(descriptorId, retrievableId, Value.Byte(result.getByte(VALUE_ATTRIBUTE_NAME)))
            is ShortDescriptor -> ShortDescriptor(descriptorId, retrievableId, Value.Short(result.getShort(VALUE_ATTRIBUTE_NAME)))
            is IntDescriptor -> IntDescriptor(descriptorId, retrievableId, Value.Int(result.getInt(VALUE_ATTRIBUTE_NAME)))
            is LongDescriptor -> LongDescriptor(descriptorId, retrievableId, Value.Long(result.getLong(VALUE_ATTRIBUTE_NAME)))
            is FloatDescriptor -> FloatDescriptor(descriptorId, retrievableId, Value.Float(result.getFloat(VALUE_ATTRIBUTE_NAME)))
            is DoubleDescriptor -> DoubleDescriptor(descriptorId, retrievableId, Value.Double(result.getDouble(VALUE_ATTRIBUTE_NAME)))
            is StringDescriptor -> StringDescriptor(descriptorId, retrievableId, Value.String(result.getString(VALUE_ATTRIBUTE_NAME)))
            is TextDescriptor -> TextDescriptor(descriptorId, retrievableId, Value.Text(result.getString(VALUE_ATTRIBUTE_NAME)))
        }
    }

    /**
     * Prepares a [SimpleFulltextPredicate] and returns a [Sequence] of [ScalarDescriptor]s.
     *
     * @param query The [SimpleFulltextPredicate] to execute.
     * @return [PreparedStatement]s.
     */
    private fun prepareFulltext(query: SimpleFulltextPredicate): PreparedStatement {
        val tableName = "\"${tableName.lowercase()}\""
        val fulltextQueryString = query.value.value.split(" ").map { "$it:*" }.joinToString(" | ") { it }
        val filter = query.filter
        if (filter == null) {
            val sql = "SELECT * FROM $tableName WHERE $VALUE_ATTRIBUTE_NAME @@ to_tsquery(?)"
            val stmt = this.connection.jdbc.prepareStatement(sql)
            stmt.setString(1, fulltextQueryString)
            return stmt
        } else {
            val sql = "SELECT * FROM $tableName WHERE $VALUE_ATTRIBUTE_NAME @@ to_tsquery(?) AND $RETRIEVABLE_ID_COLUMN_NAME = ANY(?)"
            val retrievableIds = this.resolveBooleanPredicate(filter)
            val stmt = this.connection.jdbc.prepareStatement(sql)
            stmt.setString(1, fulltextQueryString)
            stmt.setArray(2, this.connection.jdbc.createArrayOf("OTHER", retrievableIds.toTypedArray()))
            return stmt
        }
    }

    /**
     * [PreparedStatement] a [Comparison] predicate and returns a [PreparedStatement].
     *
     * @param query The [Comparison] to execute.
     * @return [PreparedStatement]s.
     */
    private fun prepareComparison(query: Comparison<*>): PreparedStatement {
        val tableName = "\"${this.tableName.lowercase()}\""
        val sql = "SELECT * FROM $tableName WHERE ${query.toWhere()}"
        val stmt = this@ScalarDescriptorReader.connection.jdbc.prepareStatement(sql)
        stmt.setValueForComparison(1, query)
        return stmt
    }
}