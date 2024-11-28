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
import java.sql.ResultSet
import java.util.*

/**
 * A [AbstractDescriptorReader] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*, *>>, connection: PgVectorConnection) : AbstractDescriptorReader<ScalarDescriptor<*, *>>(field, connection) {

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<ScalarDescriptor<*, *>> = when (val predicate = query.predicate) {
        is SimpleFulltextPredicate -> queryFulltext(predicate)
        is Comparison<*> -> queryComparison(predicate)
        else -> throw IllegalArgumentException("Query of type ${query::class} is not supported by ScalarDescriptorReader.")
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
     * Executes a [SimpleFulltextPredicate] and returns a [Sequence] of [ScalarDescriptor]s.
     *
     * @param query The [SimpleFulltextPredicate] to execute.
     * @return [Sequence] of [ScalarDescriptor]s.
     */
    private fun queryFulltext(query: SimpleFulltextPredicate): Sequence<ScalarDescriptor<*, *>> {
        val queryString = query.value.value.split(" ").map { "$it:*" }.joinToString(" | ") { it }
        val statement = "SELECT * FROM \"${tableName.lowercase()}\" WHERE ${query.attributeName} @@ to_tsquery(?)"
        return sequence {
            this@ScalarDescriptorReader.connection.jdbc.prepareStatement(statement).use { stmt ->
                stmt.setString(1, queryString)
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(rowToDescriptor(result))
                    }
                }
            }
        }
    }

    /**
     * Executes a [Comparison] and returns a [Sequence] of [ScalarDescriptor]s.
     *
     * @param query The [Comparison] to execute.
     * @return [Sequence] of [ScalarDescriptor]s.
     */
    internal fun queryComparison(query: Comparison<*>): Sequence<ScalarDescriptor<*, *>> {
        val statement = "SELECT * FROM \"$tableName\" WHERE ${query.toWhere()}"
        return sequence {
            this@ScalarDescriptorReader.connection.jdbc.prepareStatement(statement).use { stmt ->
                stmt.setValueForComparison(1, query)
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(rowToDescriptor(result))
                    }
                }
            }
        }
    }
}