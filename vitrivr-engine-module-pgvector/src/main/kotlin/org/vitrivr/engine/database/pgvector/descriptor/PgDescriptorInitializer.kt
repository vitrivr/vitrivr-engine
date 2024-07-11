package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.config.schema.IndexType
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.database.pgvector.*
import java.sql.SQLException

/**
 * An abstract implementation of a [DescriptorInitializer] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
open class PgDescriptorInitializer<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: PgVectorConnection): DescriptorInitializer<D> {

    /** The name of the table backing this [PgDescriptorInitializer]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"

    /** The [Descriptor] prototype for this [PgDescriptorWriter]. */
    protected val prototype = this.field.analyser.prototype(this.field)

    /**
     * Initializes the PostgreSQL table entity backing this [PgDescriptorInitializer].
     */
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
                Type.Byte -> statement.append("\"${field.name}\" smallint, ")
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

        /* Create entity. */
        try {
            this.connection.jdbc.prepareStatement(/* sql = postgres */ statement.toString()).use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity '$tableName' due to exception." }
            throw e
        }

        /* Create indexes (optional). */
        for (index in this.field.indexes) {
            try {
                val indexStatement = when (index.type) {
                    IndexType.SCALAR -> "CREATE INDEX IF NOT EXISTS ON $tableName (${index.attributes.joinToString(",")});"
                    IndexType.FULLTEXT -> "CREATE INDEX IF NOT EXISTS ON $tableName USING GIN(${index.attributes.joinToString(",")});"
                    IndexType.NNS ->  {
                        require(index.attributes.size == 1) { "NNS index can only be created on a single attribute." }
                        val distance = index.parameters["distance"]?.let { Distance.valueOf(it.uppercase()) } ?: Distance.EUCLIDEAN
                        "CREATE INDEX ON $tableName USING hnsw (${index.attributes.first()} ${distance.toIndexName()});"
                    }
                }
                this.connection.jdbc.prepareStatement(/* sql = postgres */ indexStatement).use { it.execute() }
            } catch (e: SQLException) {
                LOGGER.error(e) { "Failed to create index ${index.type} for entity '$tableName' due to exception." }
                throw e
            }
        }
    }

    /**
     * Checks if the schema for this [PgDescriptorInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean {
        try {
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "SELECT count(*) FROM $tableName").use {
                it.execute()
            }
        } catch (e: SQLException) {
            return false
        }
        return true
    }

    /**
     * Truncates the table backing this [PgDescriptorInitializer].
     */
    override fun truncate() {
        try {
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "TRUNCATE $tableName").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to truncate entities due to exception." }
        }
    }

    /**
     * Closes the [PgDescriptorInitializer].
     */
    private fun Distance.toIndexName() = when (this) {
        Distance.MANHATTAN -> "vector_l1_ops"
        Distance.EUCLIDEAN -> "sparsevec_l2_ops"
        Distance.COSINE -> "vector_cosine_ops"
        Distance.HAMMING -> "bit_hamming_ops"
        Distance.JACCARD -> "bit_jaccard_ops"
    }
}