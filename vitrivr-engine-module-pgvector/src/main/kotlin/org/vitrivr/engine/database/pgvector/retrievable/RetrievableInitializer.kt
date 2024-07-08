package org.vitrivr.engine.database.pgvector.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.pgvector.*
import java.sql.Connection
import java.sql.SQLException

/**
 * A [RetrievableInitializer] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableInitializer(private val connection: Connection): RetrievableInitializer {
    /**
     * Initializes the [RetrievableInitializer].
     */
    override fun initialize() {
        try {
            /* Create 'retrievable' entity. */
            this.connection.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS $RETRIEVABLE_ENTITY_NAME ($RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, type VARCHAR(100), PRIMARY KEY ($RETRIEVABLE_ID_COLUMN_NAME));").use {
                it.execute()
            }

            /* Create 'relationship' entity. */
            this.connection.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS $RELATIONSHIP_ENTITY_NAME ($OBJECT_ID_COLUMN_NAME uuid NOT NULL, $PREDICATE_COLUMN_NAME VARCHAR(100) NOT NULL, $SUBJECT_ID_COLUMN_NAME uuid NOT NULL, PRIMARY KEY ($OBJECT_ID_COLUMN_NAME, $PREDICATE_COLUMN_NAME, $SUBJECT_ID_COLUMN_NAME), FOREIGN KEY($OBJECT_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME), FOREIGN KEY($SUBJECT_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME));")
                .use {
                    it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity due to exception." }
        }
    }

    /**
     * Checks if the schema for this [RetrievableInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean {
        try {
            this.connection.prepareStatement(/* sql = postgres */ "SELECT count(*) FROM $RETRIEVABLE_ENTITY_NAME").use {
                it.execute()
            }
        } catch (e: SQLException) {
            return false
        }
        try {
            this.connection.prepareStatement(/* sql = postgres */ "SELECT count(*) FROM $RELATIONSHIP_ENTITY_NAME").use {
                it.execute()
            }
        } catch (e: SQLException) {
            return false
        }
        return true
    }

    /**
     * Truncates the entity that is used to store [Retrievable]s and Relationships in PostgreSQL with pgVector.
     */
    override fun truncate() {
        try {
            this.connection.prepareStatement(/* sql = postgres */ "TRUNCATE $RETRIEVABLE_ENTITY_NAME, $RELATIONSHIP_ENTITY_NAME").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to truncate entities due to exception." }
        }
    }
}