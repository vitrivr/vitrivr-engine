package org.vitrivr.engine.database.pgvector.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.pgvector.*
import java.sql.SQLException

/**
 * A [RetrievableInitializer] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableInitializer(private val connection: PgVectorConnection): RetrievableInitializer {
    /**
     * Initializes the data structures backing this [RetrievableInitializer].
     */
    override fun initialize() {
        try {
            /* Create 'retrievable' entity and index. */
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS ${RETRIEVABLE_ENTITY_NAME} ($RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, type VARCHAR(100), PRIMARY KEY ($RETRIEVABLE_ID_COLUMN_NAME));").use {
                it.execute()
            }

            /* Create 'relationship' entity. */
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS ${RELATIONSHIP_ENTITY_NAME} ($SUBJECT_ID_COLUMN_NAME uuid NOT NULL, $PREDICATE_COLUMN_NAME VARCHAR(100) NOT NULL, $OBJECT_ID_COLUMN_NAME uuid NOT NULL, PRIMARY KEY ($SUBJECT_ID_COLUMN_NAME, $PREDICATE_COLUMN_NAME, $OBJECT_ID_COLUMN_NAME), FOREIGN KEY($OBJECT_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME) ON DELETE CASCADE, FOREIGN KEY($SUBJECT_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME) ON DELETE CASCADE);").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity due to exception." }
        }
    }

    /**
     * De-initializes the data structures backing this [RetrievableInitializer].
     */
    override fun deinitialize() {
        try {
            /* Create 'retrievable' entity and index. */
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "DROP TABLE IF EXISTS ${RETRIEVABLE_ENTITY_NAME} CASCADE;").use {
                it.execute()
            }

            /* Create 'relationship' entity. */
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "DROP TABLE IF EXISTS ${RELATIONSHIP_ENTITY_NAME} CASCADE;").use {
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
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "SELECT count(*) FROM  ${RETRIEVABLE_ENTITY_NAME};").use {
                it.execute()
            }
        } catch (e: SQLException) {
            return false
        }
        try {
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "SELECT count(*) FROM $RELATIONSHIP_ENTITY_NAME;").use {
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
            this.connection.jdbc.prepareStatement(/* sql = postgres */ "TRUNCATE ${RETRIEVABLE_ENTITY_NAME}, ${RELATIONSHIP_ENTITY_NAME}").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to truncate entities due to exception." }
        }
    }
}