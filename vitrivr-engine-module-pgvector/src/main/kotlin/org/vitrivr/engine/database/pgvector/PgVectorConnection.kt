package org.vitrivr.engine.database.pgvector

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.PGConnection
import org.vitrivr.engine.core.database.AbstractConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import java.sql.Connection
import java.sql.SQLException


/** Defines [KLogger] of the class. */
internal val LOGGER: KLogger = logger("org.vitrivr.engine.database.pgvector.PgVectorConnection")

/**
 * An [AbstractConnection] to connect to a PostgreSQL instance with the pgVector extension.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class PgVectorConnection(provider: PgVectorConnectionProvider, schemaName: String, internal val database: Database) : AbstractConnection(schemaName, provider) {

    /** The exposed [Schema] used by this [PgVectorConnection]. */
    internal val schema: Schema = Schema(schemaName.lowercase())

    val jdbc = this.database.connector().connection as Connection

    init {
        transaction(this.database) {
            /* Make sure that the pg_vector extension is installed. */
            try {
                exec("CREATE EXTENSION IF NOT EXISTS vector;")
            } catch (e: Throwable) {
                LOGGER.error(e) { "Failed to create extension pg_vector due to exception." }
                throw e
            }

            /* Create schema if it does not exist. */
            try {
                SchemaUtils.createSchema(this@PgVectorConnection.schema)
            } catch (e: SQLException) {
                if (e.sqlState == "42P06") {
                    LOGGER.info { "Schema '$schemaName' already exists." }
                } else {
                    LOGGER.error(e) { "Failed to create schema '$schemaName' due to exception." }
                    throw e
                }
            }

            /* Set default schema. */
            try {
                SchemaUtils.setSchema(this@PgVectorConnection.schema)
            } catch (e: SQLException) {
                LOGGER.error(e) { "Failed to set search path '$schemaName' due to exception." }
                throw e
            }
        }

        /* Register the vector data type. */
        (this.database.connector().connection as? PGConnection)?.addDataType("vector", PgVector::class.java)
        (this.database.connector().connection as? PGConnection)?.addDataType("bit", PgBitVector::class.java)
    }

    /**
     * Tries to execute a given action within a database transaction.
     *
     * @param action The action to execute within the transaction.
     */
    @Synchronized
    override fun <T> withTransaction(action: () -> T): T = transaction {
        action()
    }

    /**
     * Generates and returns a [RetrievableInitializer] for this [PgVectorConnection].
     *
     * @return [RetrievableInitializer]
     */
    override fun getRetrievableInitializer(): RetrievableInitializer
        = org.vitrivr.engine.database.pgvector.retrievable.RetrievableInitializer(this)

    /**
     * Generates and returns a [RetrievableWriter] for this [PgVectorConnection].
     *
     * @return [RetrievableWriter]
     */
    override fun getRetrievableWriter(): RetrievableWriter
        = org.vitrivr.engine.database.pgvector.retrievable.RetrievableWriter(this)

    /**
     * Generates and returns a [RetrievableWriter] for this [PgVectorConnection].
     *
     * @return [RetrievableReader]
     */
    override fun getRetrievableReader(): RetrievableReader
        = org.vitrivr.engine.database.pgvector.retrievable.RetrievableReader(this)

    /**
     * Returns the human-readable description of this [PgVectorConnection].
     */
    override fun description(): String = this.database.url

    /**
     * Closes this [PgVectorConnection]
     */
    override fun close()  {
        /* No op. */
    }
}