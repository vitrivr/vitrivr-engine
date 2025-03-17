package org.vitrivr.engine.database.pgvector.descriptor

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.tables.AbstractDescriptorTable
import java.sql.SQLException

/**
 * An abstract implementation of a [DescriptorInitializer] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
open class PgDescriptorInitializer<D : Descriptor<*>>(
    final override val field: Schema.Field<*, D>,
    protected val connection: PgVectorConnection
) : DescriptorInitializer<D> {

    companion object {
        /** Set of scalar index structures supported by PostgreSQL. */
        val INDEXES_SCALAR = setOf("btree", "brin", "hash")

        /** Set of NNS index structures supported by PostgreSQL. */
        val INDEXES_NNS = setOf("hnsw", "ivfflat")

        /** Set of FULLTEXT index structures supported by PostgreSQL. */
        val INDEXES_FULLTEXT = setOf("gin")
    }

    /** The [AbstractDescriptorTable] backing this [PgDescriptorInitializer]. */
    protected val table: AbstractDescriptorTable<D> = this.field.toTable()

    /** The name of the table backing this [PgDescriptorInitializer]. */
    protected val tableName: String
        get() = this.table.nameInDatabaseCase()

    /**
     * Initializes the PostgreSQL table entity backing this [PgDescriptorInitializer].
     */
    override fun initialize() = transaction(this.connection.database) {
        try {
            SchemaUtils.create(this@PgDescriptorInitializer.table)
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to initialize entity '${tableName}' due to exception." }
            throw e
        }
    }

    /**
     * De-initializes the PostgreSQL table entity backing this [PgDescriptorInitializer].
     */
    override fun deinitialize() = transaction(this.connection.database) {
        try {
            SchemaUtils.drop(this@PgDescriptorInitializer.table)
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity '${tableName}' due to exception." }
            throw e
        }
    }

    /**
     * Checks if the schema for this [PgDescriptorInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean = try {
        transaction(this.connection.database) {
            SchemaUtils.listTables().let {
                val tables = it.map { table -> table.split(".").last() }
                tables.contains(this@PgDescriptorInitializer.table.nameInDatabaseCase())
            }
        }
    } catch (e: Throwable) {
        false
    }

    /**
     * Truncates the table backing this [PgDescriptorInitializer].
     */
    override fun truncate() = transaction(this.connection.database) {
        try {
            exec("TRUNCATE TABLE IF NOT EXISTS ${this@PgDescriptorInitializer.tableName};")
            Unit
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to truncate entities due to exception." }
            throw e
        }
    }

    /**
     * Closes the [PgDescriptorInitializer].
     */
    private fun Distance.toIndexName() = when (this) {
        Distance.MANHATTAN -> "vector_l1_ops"
        Distance.EUCLIDEAN -> "vector_l2_ops"
        Distance.COSINE -> "vector_cosine_ops"
        Distance.HAMMING -> "bit_hamming_ops"
        Distance.JACCARD -> "bit_jaccard_ops"
    }
}