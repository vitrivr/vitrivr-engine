package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorReader
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import java.sql.ResultSet
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * An [AbstractDescriptorReader] for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorReader(field: Schema.Field<*, StructDescriptor>, connection: PgVectorConnection) : AbstractDescriptorReader<StructDescriptor>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<StructDescriptor> = when (query) {
        is SimpleFulltextQuery -> queryFulltext(query)
        is SimpleBooleanQuery<*> -> queryBoolean(query)
        else -> throw IllegalArgumentException("Query of typ ${query::class} is not supported by StructDescriptorReader.")
    }

    /**
     * Converts the provided [ResultSet] to a [StructDescriptor].
     *
     * @param result The [ResultSet] to convert.
     * @return The resulting [StructDescriptor].
     */
    override fun rowToDescriptor(result: ResultSet): StructDescriptor {
        val constructor = this.field.analyser.descriptorClass.primaryConstructor ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val values = TreeMap<AttributeName,Value<*>?>()
        val parameters: MutableList<Any?> = mutableListOf(
            result.getObject(DESCRIPTOR_ID_COLUMN_NAME, UUID::class.java) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'."),
            result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'."),
            values,
            this.field
        )

        /* Append dynamic parameters of struct. */
        for (field in this.prototype.layout()) {
            values[field.name] = when(field.type) {
                Type.String -> result.getString(field.name.lowercase())?.let { Value.String(it) }
                Type.Text -> result.getString(field.name.lowercase())?.let { Value.Text(it) }
                Type.Boolean -> result.getBoolean(field.name.lowercase()).let { Value.Boolean(it) }
                Type.Byte -> result.getByte(field.name.lowercase()).let { Value.Byte(it) }
                Type.Short -> result.getShort(field.name.lowercase()).let { Value.Short(it) }
                Type.Int -> result.getInt(field.name.lowercase()).let { Value.Int(it) }
                Type.Long -> result.getLong(field.name.lowercase()).let { Value.Long(it) }
                Type.Float -> result.getFloat(field.name.lowercase()).let { Value.Float(it) }
                Type.Double -> result.getDouble(field.name.lowercase()).let { Value.Double(it) }
                Type.Datetime -> result.getDate(field.name.lowercase()).toInstant().let { Value.DateTime(Date(it.toEpochMilli())) }
                Type.UUID -> result.getObject(field.name.lowercase(), UUID::class.java).let { Value.UUIDValue(it) }
                is Type.BooleanVector -> result.getObject(field.name.lowercase(), PgBitVector::class.java).toBooleanVector()
                is Type.IntVector -> result.getObject(field.name.lowercase(), PgVector::class.java)?.toIntVector()
                is Type.LongVector -> result.getObject(field.name.lowercase(), PgVector::class.java)?.toLongVector()
                is Type.FloatVector -> result.getObject(field.name.lowercase(), PgVector::class.java)?.toFloatVector()
                is Type.DoubleVector -> result.getObject(field.name.lowercase(), PgVector::class.java)?.toDoubleVector()
            } as Value<*>?
        }

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }

    /**
     * Executes a [SimpleFulltextQuery] and returns a [Sequence] of [StructDescriptor]s.
     *
     * @param query The [SimpleFulltextQuery] to execute.
     * @return [Sequence] of [StructDescriptor]s.
     */
    private fun queryFulltext(query: SimpleFulltextQuery): Sequence<StructDescriptor> {
        require(query.attributeName != null) { "Query attribute must not be null for a fulltext query on a struct descriptor." }
        val statement = "SELECT * FROM \"${tableName.lowercase()}\" WHERE ${query.attributeName} @@ plainto_tsquery(?)"
        return sequence {
            this@StructDescriptorReader.connection.jdbc.prepareStatement(statement).use { stmt ->
                stmt.setString(1, query.value.value)
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(rowToDescriptor(result))
                    }
                }
            }
        }
    }

    /**
     * Executes a [SimpleBooleanQuery] and returns a [Sequence] of [StructDescriptor]s.
     *
     * @param query The [SimpleBooleanQuery] to execute.
     * @return [Sequence] of [StructDescriptor]s.
     */
    private fun queryBoolean(query: SimpleBooleanQuery<*>): Sequence<StructDescriptor> {
        require(query.attributeName != null) { "Query attribute must not be null for a fulltext query on a struct descriptor." }
        val statement = "SELECT * FROM field.name.lowercase() WHERE ${query.attributeName} ${query.comparison.toSql()} ?"
        return sequence {
            this@StructDescriptorReader.connection.jdbc.prepareStatement(statement).use { stmt ->
                stmt.setValue(1, query.value)
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(rowToDescriptor(result))
                    }
                }
            }
        }
    }
}