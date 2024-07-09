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
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
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
 * @version 1.1.0
 */
class StructDescriptorReader(field: Schema.Field<*, StructDescriptor>, connection: PgVectorConnection) : AbstractDescriptorReader<StructDescriptor>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<StructDescriptor> {
        try {
            /* Prepare statement based on query. */
            val statement = when (query) {
                is SimpleFulltextQuery -> TODO()
                is SimpleBooleanQuery<*> -> {
                    require(query.attributeName != null) { "Boolean query on a struct field requires specification of a field's attribute name." }
                    this.connection.jdbc.prepareStatement("SELECT * FROM $tableName WHERE ${query.attributeName} = ?;").apply {
                        setString(1, query.value.toString())
                    }
                }
                else -> throw IllegalArgumentException("Query of typ ${query::class} is not supported by StringDescriptorReader.")
            }

            /* Execute statement and return it. */
            return statement.executeAndStream()
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to execute query on '$tableName' due to SQL error." }
            return emptySequence()
        }
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
            values
        )

        /* Append dynamic parameters of struct. */
        for (field in this.prototype.schema()) {
            values[field.name] = when(field.type) {
                Type.String -> result.getString(field.name)?.let { Value.String(it) }
                Type.Text -> result.getString(field.name)?.let { Value.Text(it) }
                Type.Boolean -> result.getBoolean(field.name).let { Value.Boolean(it) }
                Type.Byte -> result.getByte(field.name).let { Value.Byte(it) }
                Type.Short -> result.getShort(field.name).let { Value.Short(it) }
                Type.Int -> result.getInt(field.name).let { Value.Int(it) }
                Type.Long -> result.getLong(field.name).let { Value.Long(it) }
                Type.Float -> result.getFloat(field.name).let { Value.Float(it) }
                Type.Double -> result.getDouble(field.name).let { Value.Double(it) }
                Type.Datetime -> result.getDate(field.name).toInstant().let { Value.DateTime(Date(it.toEpochMilli())) }
                is Type.BooleanVector -> result.getObject(field.name, PgBitVector::class.java).toBooleanVector()
                is Type.IntVector -> result.getObject(field.name, PgVector::class.java)?.toIntVector()
                is Type.LongVector -> result.getObject(field.name, PgVector::class.java)?.toLongVector()
                is Type.FloatVector -> result.getObject(field.name, PgVector::class.java)?.toFloatVector()
                is Type.DoubleVector -> result.getObject(field.name, PgVector::class.java)?.toDoubleVector()
            } as Value<*>?
        }

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }
}