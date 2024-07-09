package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorInitializer
import java.sql.SQLException

/**
 * A [AbstractDescriptorInitializer] implementation for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorInitializer(field: Schema.Field<*, StructDescriptor>, connection: PgVectorConnection) : AbstractDescriptorInitializer<StructDescriptor>(field, connection.jdbc) {
    override fun initialize() {
        val statement = StringBuilder("CREATE TABLE IF NOT EXISTS $tableName(")
        statement.append("$DESCRIPTOR_ID_COLUMN_NAME uuid NOT NULL, ")
        statement.append("$RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, ")

        /* Add columns for each field in the struct. */
        for (field in this.prototype.schema()) {
            when (field.type) {
                Type.String -> statement.append("\"${field.name}\" varchar(255), ")
                Type.Text -> statement.append("\"${field.name}\" text, ")
                Type.Boolean -> statement.append("\"${field.name}\" boolean, ")
                Type.Byte -> statement.append("$\"{field.name}\" smallint, ")
                Type.Short -> statement.append("\"${field.name}\" smallint, ")
                Type.Int -> statement.append("\"${field.name}\" integer, ")
                Type.Long -> statement.append("\"${field.name}\" bigint, ")
                Type.Float -> statement.append("\"${field.name}\" real, ")
                Type.Double -> statement.append("\"${field.name}\" double precision, ")
                Type.Datetime -> statement.append("\"${field.name}\" datetime, ")
                is Type.BooleanVector -> statement.append("\"${field.name}\" bit(${field.type.dimensions}), ")
                is Type.DoubleVector -> statement.append("\"${field.name}\" vector(${field.type.dimensions}), ")
                is Type.FloatVector -> statement.append("\"${field.name}\" vector(${field.type.dimensions}), ")
                is Type.IntVector -> statement.append("\"${field.name}\" vector(${field.type.dimensions}), ")
                is Type.LongVector -> statement.append("\"${field.name}\" vector(${field.type.dimensions}), ")
            }
        }

        /* Finalize statement*/
        statement.append("PRIMARY KEY ($DESCRIPTOR_ID_COLUMN_NAME), ")
        statement.append("FOREIGN KEY ($RETRIEVABLE_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME));")

        try {
            /* Create 'retrievable' entity. */
            this.connection.prepareStatement(/* sql = postgres */ statement.toString()).use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity '$tableName' due to exception." }
        }
    }
}