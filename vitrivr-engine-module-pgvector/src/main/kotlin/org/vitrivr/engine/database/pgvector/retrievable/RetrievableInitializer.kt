package org.vitrivr.engine.database.pgvector.retrievable

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.tables.RelationshipTable
import org.vitrivr.engine.database.pgvector.tables.RetrievableTable
import java.sql.SQLException

/**
 * A [RetrievableInitializer] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
internal class RetrievableInitializer(private val connection: PgVectorConnection): RetrievableInitializer {
    /**
     * Initializes the data structures backing this [RetrievableInitializer].
     */
    override fun initialize() {
        try {
            transaction(this.connection.database) {
                SchemaUtils.create(RetrievableTable)
                SchemaUtils.create(RelationshipTable)
            }
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to initialize retrievable entities due to exception." }
        }
    }

    /**
     * De-initializes the data structures backing this [RetrievableInitializer].
     */
    override fun deinitialize() {
        try {
            transaction(this.connection.database) {
                SchemaUtils.drop(RelationshipTable, RetrievableTable)
            }
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to de-initialize retrievable entities due to exception." }
        }
    }

    /**
     * Checks if the schema for this [RetrievableInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean = try {
        transaction(this.connection.database) {
            SchemaUtils.listTables().let {
                val tables = it.map { table -> table.split(".").last() }
                tables.contains(RetrievableTable.nameInDatabaseCase()) && tables.contains(RelationshipTable.nameInDatabaseCase())
            }
        }
    } catch (e: Throwable) {
        false
    }

    /**
     * Truncates the entity that is used to store [Retrievable]s and Relationships in PostgreSQL with pgVector.
     */
    override fun truncate() {
        try {
            transaction(this.connection.database) {
                exec("TRUNCATE TABLE IF EXISTS ${RetrievableTable.nameInDatabaseCase()};")
            }

        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to truncate entities due to exception." }
        }
    }
}