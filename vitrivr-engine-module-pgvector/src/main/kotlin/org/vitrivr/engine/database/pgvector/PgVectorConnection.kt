package org.vitrivr.engine.database.pgvector

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging.logger
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
 * A [AbstractConnection] to connect to a PostgreSQL instance with the pgVector extension.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PgVectorConnection(provider: PgVectorConnectionProvider, schemaName: String, internal val connection: Connection): AbstractConnection(schemaName, provider) {

    init {
        /* Make sure that the pg_vector extension is installed. */
        try {
            this.connection.prepareStatement("CREATE EXTENSION IF NOT EXISTS vector;").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to create extension pg_vector due to exception." }
            throw e
        }

        /* Register the vector data type. */
        this.connection.unwrap(PGConnection::class.java).addDataType("vector", PgVector::class.java)
        this.connection.unwrap(PGConnection::class.java).addDataType("bit", PgBitVector::class.java)

        /* Create necessary database. */
        try {
            this.connection.prepareStatement("CREATE DATABASE $schemaName;").use {
                it.execute()
            }
        } catch (e: SQLException) {
            if (e.sqlState == "42P04") {
                LOGGER.info { "Database '$schemaName' already exists." }
            } else {
                LOGGER.error(e) { "Failed to create database '$schemaName' due to exception." }
                throw e
            }
        }
    }

    /**
     * Generates and returns a [RetrievableInitializer] for this [PgVectorConnection].
     *
     * @return [RetrievableInitializer]
     */
    override fun getRetrievableInitializer(): RetrievableInitializer
        = org.vitrivr.engine.database.pgvector.retrievable.RetrievableInitializer(this.connection)

    /**
     * Generates and returns a [RetrievableWriter] for this [PgVectorConnection].
     *
     * @return [RetrievableWriter]
     */
    override fun getRetrievableWriter(): RetrievableWriter
        = org.vitrivr.engine.database.pgvector.retrievable.RetrievableWriter(this.connection)

    /**
     * Generates and returns a [RetrievableWriter] for this [PgVectorConnection].
     *
     * @return [RetrievableReader]
     */
    override fun getRetrievableReader(): RetrievableReader
        = org.vitrivr.engine.database.pgvector.retrievable.RetrievableReader(this.connection)

    /**
     * Returns the human-readable description of this [PgVectorConnection].
     */
    override fun description(): String = this.connection.toString()

    /**
     * Closes this [PgVectorConnection]
     */
    override fun close() {
        try {
            this.connection.close()
        } catch (e: SQLException) {
            LOGGER.warn(e) { "Failed to close database connection due to exception." }
        }
    }
}