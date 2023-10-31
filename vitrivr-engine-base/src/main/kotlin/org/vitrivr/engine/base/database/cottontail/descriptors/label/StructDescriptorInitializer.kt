package org.vitrivr.engine.base.database.cottontail.descriptors.label

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [AbstractDescriptorInitializer] implementation for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorInitializer(field: Schema.Field<*, StructDescriptor>, connection: CottontailConnection) : AbstractDescriptorInitializer<StructDescriptor>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        /* Prepare query. */
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName(CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = false, autoIncrement = false)


        for (field in this.field.analyser.prototype().schema()) {
            require(field.dimensions.size <= 1) { "Cottontail DB currently doesn't support tensor types."}
            val vector = field.dimensions.size == 1
            val type = when (field.type) {
                FieldType.STRING -> Types.String
                FieldType.BYTE -> Types.Byte
                FieldType.SHORT -> Types.Short
                FieldType.BOOLEAN -> if (vector) { Types.BooleanVector(field.dimensions[0]) } else { Types.Boolean }
                FieldType.INT -> if (vector) { Types.IntVector(field.dimensions[0]) } else { Types.Int }
                FieldType.LONG -> if (vector) { Types.LongVector(field.dimensions[0]) } else { Types.Long }
                FieldType.FLOAT -> if (vector) { Types.FloatVector(field.dimensions[0]) } else { Types.Float }
                FieldType.DOUBLE -> if (vector) { Types.DoubleVector(field.dimensions[0]) } else { Types.Double }
            }
            create.column(Name.ColumnName(field.name), type, nullable = field.nullable, primaryKey = false, autoIncrement = false)
        }

        try {
            /* Try to create entity. */
            this.connection.client.create(create)
        } catch (e: StatusException) {
            logger.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }
}