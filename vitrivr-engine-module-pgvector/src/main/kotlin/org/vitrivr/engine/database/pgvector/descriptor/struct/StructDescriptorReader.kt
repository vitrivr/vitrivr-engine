package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorReader
import java.sql.ResultSet
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * An [AbstractDescriptorReader] for [LabelDescriptor]s.
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
                    this.connection.connection.prepareStatement("SELECT * FROM $tableName WHERE ${query.attributeName} = ?;").apply {
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
        val parameters: MutableList<Any?> = mutableListOf(
            result.getObject(DESCRIPTOR_ID_COLUMN_NAME, UUID::class.java) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'."),
            result.getObject(DESCRIPTOR_ID_COLUMN_NAME, UUID::class.java) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'."),
        )

        /* Append dynamic parameters of struct. */
        for (field in this.prototype.schema()) {
            parameters.add(
                when(field.type) {
                    Type.STRING -> result.getString(field.name)
                    Type.BOOLEAN -> result.getBoolean(field.name)
                    Type.BYTE -> result.getByte(field.name)
                    Type.SHORT -> result.getShort(field.name)
                    Type.INT -> result.getInt(field.name)
                    Type.LONG -> result.getLong(field.name)
                    Type.FLOAT ->  result.getFloat(field.name)
                    Type.DOUBLE -> result.getDouble(field.name)
                    Type.DATETIME -> result.getDate(field.name).toLocalDate()
                }
            )
        }

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }
}