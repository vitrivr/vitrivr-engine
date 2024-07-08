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
class StructDescriptorInitializer(field: Schema.Field<*, StructDescriptor>, connection: PgVectorConnection) : AbstractDescriptorInitializer<StructDescriptor>(field, connection.connection) {
    override fun initialize() {
        val statement = StringBuilder("CREATE TABLE IF NOT EXISTS $tableName(")
        statement.append("$DESCRIPTOR_ID_COLUMN_NAME uuid NOT NULL, ")
        statement.append("$RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, ")

        /* Add columns for each field in the struct. */
        for (field in this.field.analyser.prototype(this.field).schema()) {
            require(field.dimensions.size <= 1) { "Cottontail DB currently doesn't support tensor types."}
            when (field.type) {
                Type.STRING -> statement.append("\"${field.name}\" varchar(255), ")
                Type.BOOLEAN -> statement.append("\"${field.name}\" boolean, ")
                Type.BYTE -> statement.append("$\"{field.name}\" smallint, ")
                Type.SHORT -> statement.append("\"${field.name}\" smallint, ")
                Type.INT -> statement.append("\"${field.name}\" integer, ")
                Type.LONG -> statement.append("\"${field.name}\" bigint, ")
                Type.FLOAT -> statement.append("\"${field.name}\" real, ")
                Type.DOUBLE -> statement.append("\"${field.name}\" double precision, ")
                Type.DATETIME -> statement.append("\"${field.name}\" datetime, ")
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