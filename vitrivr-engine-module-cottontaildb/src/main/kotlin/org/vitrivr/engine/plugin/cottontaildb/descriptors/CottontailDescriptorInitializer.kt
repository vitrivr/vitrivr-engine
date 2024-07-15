package org.vitrivr.engine.plugin.cottontaildb.descriptors

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.ddl.*
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.grpc.CottontailGrpc
import org.vitrivr.engine.core.config.schema.IndexType
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.*
import java.sql.SQLException

/**
 * An abstract implementation of a [DescriptorInitializer] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
open class CottontailDescriptorInitializer<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: CottontailConnection) : DescriptorInitializer<D> {
    /** The [Name.EntityName] used by this [Descriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName.create(this.field.schema.name, "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Initializes the Cottontail DB entity backing this [CottontailDescriptorInitializer].
     */
    override fun initialize() {
        /* Prepare query. */
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName.create(DESCRIPTOR_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName.create(RETRIEVABLE_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = false, autoIncrement = false)

        /* Append fields. */
        for (field in this.field.analyser.prototype(this.field).layout()) {
            create.column(Name.ColumnName.create(field.name), field.type.toCottontailType(), nullable = field.nullable, primaryKey = false, autoIncrement = false)
        }

        try {
            /* Try to create entity. */
            this.connection.client.create(create)
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to initialize entity '${this.entityName}' due to exception." }
        }

        /* Create indexes (optional). */
        for (index in this.field.indexes) {
            require(index.attributes.size == 1) { "Cottontail DB currently only supports single-column indexes." }
            try {
                val createIndex = when (index.type) {
                    IndexType.SCALAR -> CreateIndex(this.entityName, CottontailGrpc.IndexType.BTREE).column(index.attributes.first())
                    IndexType.FULLTEXT -> CreateIndex(this.entityName, CottontailGrpc.IndexType.LUCENE).column(index.attributes.first())
                    IndexType.NNS -> CreateIndex(this.entityName, CottontailGrpc.IndexType.PQ).column(index.attributes.first())
                }
                this.connection.client.create(createIndex)
            } catch (e: SQLException) {
                LOGGER.error(e) { "Failed to create index ${index.type} for entity '$entityName' due to exception." }
                throw e
            }
        }
    }

    /**
     * Initializes the Cottontail DB entity backing this [CottontailDescriptorInitializer].
     */
    override fun deinitialize() {
        try {
            /* Try to drop entity. */
            this.connection.client.drop(DropEntity(this.entityName))
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to de-initialize entity '${this.entityName}' due to exception." }
        }
    }

    /**
     * Checks if the schema for this [CottontailDescriptorInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean = try {
        this.connection.client.list(ListEntities(this.entityName.schema)).asSequence().any {
            Name.EntityName.parse(it.asString(0)!!) == this.entityName
        }
    } catch (e: StatusRuntimeException) {
        LOGGER.error(e) { "Failed to check initialization status of entity '${this.entityName}' due to exception." }
        false
    }

    /**
     * Truncates the entity backing this [CottontailDescriptorInitializer].
     */
    override fun truncate() {
        val truncate = TruncateEntity(this.entityName)
        try {
            this.connection.client.truncate(truncate).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to truncate entity '${this.entityName}' due to exception." }
        }
    }
}