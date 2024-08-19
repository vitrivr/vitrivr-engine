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
 * An [AbstractConnection] to connect to a PostgreSQL instance with the pgVector extension.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PgVectorConnection(provider: PgVectorConnectionProvider, schemaName: String, val jdbc: Connection) : AbstractConnection(schemaName, provider) {

    init {
        /* Make sure that the pg_vector extension is installed. */
        try {
            this.jdbc.prepareStatement("CREATE EXTENSION IF NOT EXISTS vector;").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to create extension pg_vector due to exception." }
            throw e
        }

        /* Create necessary schema. */
        try {
            this.jdbc.prepareStatement("CREATE SCHEMA \"${schemaName}\";").use {
                it.execute()
            }
        } catch (e: SQLException) {
            if (e.sqlState == "42P06") {
                LOGGER.info { "Schema '$schemaName' already exists." }
            } else {
                LOGGER.error(e) { "Failed to create schema '$schemaName' due to exception." }
                throw e
            }
        }

        try {
            this.jdbc.prepareStatement("SET search_path TO \"$schemaName\", public;").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to set search path '$schemaName' due to exception." }
            throw e
        }

        /* Register the vector data type. */
        this.jdbc.unwrap(PGConnection::class.java).addDataType("vector", PgVector::class.java)
        this.jdbc.unwrap(PGConnection::class.java).addDataType("bit", PgBitVector::class.java)
    }

    /**
     * Tries to execute a given action within a database transaction.
     *
     * @param action The action to execute within the transaction.
     */
    @Synchronized
    override fun <T> withTransaction(action: (Unit) -> T): T {
        try {
            this.jdbc.autoCommit = false
            val ret = action.invoke(Unit)
            this.jdbc.commit()
            return ret
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to execute transaction due to exception." }
            this.jdbc.rollback()
            throw e
        } finally {
            this.jdbc.autoCommit = true
        }
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
    override fun description(): String = this.jdbc.toString()

    /**
     * Closes this [PgVectorConnection]
     */
    override fun close() {
        try {
            this.jdbc.close()
        } catch (e: SQLException) {
            LOGGER.warn(e) { "Failed to close database connection due to exception." }
        }
    }
}