package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.config.schema.IndexType
import org.vitrivr.engine.core.database.Initializer.Companion.DISTANCE_PARAMETER_NAME
import org.vitrivr.engine.core.database.Initializer.Companion.INDEX_TYPE_PARAMETER_NAME
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

    companion object {
        /** Set of scalar index structures supported by PostgreSQL. */
        private val INDEXES_SCALAR = setOf("btree", "brin", "hash")

        /** Set of NNS index structures supported by PostgreSQL. */
        private val INDEXES_NNS = setOf("hnsw", "ivfflat")
    }

    /** The name of the table backing this [PgDescriptorInitializer]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"

    /** The [Descriptor] prototype for this [PgDescriptorWriter]. */
    protected val prototype = this.field.analyser.prototype(this.field)

    /**
     * Initializes the PostgreSQL table entity backing this [PgDescriptorInitializer].
     */
    override fun initialize() {
        val statement = StringBuilder("CREATE TABLE IF NOT EXISTS ${tableName} (")
        statement.append("$DESCRIPTOR_ID_COLUMN_NAME uuid NOT NULL, ")
        statement.append("$RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, ")

        /* Add columns for each field in the struct. */
        for (field in this.prototype.layout()) {
            when (field.type) {
                Type.String -> statement.append("\"${field.name.lowercase()}\" varchar(255), ")
                Type.Text -> statement.append("\"${field.name.lowercase()}\" text, ")
                Type.Boolean -> statement.append("\"${field.name.lowercase()}\" boolean, ")
                Type.Byte -> statement.append("\"${field.name.lowercase()}\" smallint, ")
                Type.Short -> statement.append("\"${field.name.lowercase()}\" smallint, ")
                Type.Int -> statement.append("\"${field.name.lowercase()}\" integer, ")
                Type.Long -> statement.append("\"${field.name.lowercase()}\" bigint, ")
                Type.Float -> statement.append("\"${field.name.lowercase()}\" real, ")
                Type.Double -> statement.append("\"${field.name.lowercase()}\" double precision, ")
                Type.Datetime -> statement.append("\"${field.name.lowercase()}\" datetime, ")
                Type.UUID -> statement.append("\"${field.name.lowercase()}\" uuid, ")
                is Type.BooleanVector -> statement.append("\"${field.name.lowercase()}\" bit(${field.type.dimensions}), ")
                is Type.DoubleVector -> statement.append("\"${field.name.lowercase()}\" vector(${field.type.dimensions}), ")
                is Type.FloatVector -> statement.append("\"${field.name.lowercase()}\" vector(${field.type.dimensions}), ")
                is Type.IntVector -> statement.append("\"${field.name.lowercase()}\" vector(${field.type.dimensions}), ")
                is Type.LongVector -> statement.append("\"${field.name.lowercase()}\" vector(${field.type.dimensions}), ")
            }
        }

        /* Finalize statement*/
        statement.append("PRIMARY KEY ($DESCRIPTOR_ID_COLUMN_NAME), ")
        statement.append("FOREIGN KEY ($RETRIEVABLE_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME) ON DELETE CASCADE);")

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
                    IndexType.SCALAR -> {
                        val type = index.parameters[INDEX_TYPE_PARAMETER_NAME]?.lowercase() ?: "btree"
                        require(type in INDEXES_SCALAR) { "Index type '$type' is not supported by PostgreSQL." }
                        "CREATE INDEX ON $tableName USING $type(${index.attributes.joinToString(",")});"
                    }
                    IndexType.FULLTEXT -> "CREATE INDEX ON $tableName USING gin(${index.attributes.joinToString(",") { "to_tsvector('${index.parameters["language"] ?: "english"}', $it)" }});"
                    IndexType.NNS ->  {
                        require(index.attributes.size == 1) { "NNS index can only be created on a single attribute." }
                        val type = index.parameters[INDEX_TYPE_PARAMETER_NAME]?.lowercase() ?: "hnsw"
                        val distance = index.parameters[DISTANCE_PARAMETER_NAME]?.let { Distance.valueOf(it.uppercase()) } ?: Distance.EUCLIDEAN
                        require(type in INDEXES_NNS) { "Index type '$type' is not supported by PostgreSQL." }
                        "CREATE INDEX ON $tableName USING $type(${index.attributes.first()} ${distance.toIndexName()});"
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
     * De-initializes the PostgreSQL table entity backing this [PgDescriptorInitializer].
     */
    override fun deinitialize() {
        try {
            /* Create 'retrievable' entity and index. */
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "DROP TABLE IF EXISTS ${tableName} CASCADE;").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to de-initialize entity '$tableName' due to exception." }
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