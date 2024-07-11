package org.vitrivr.engine.database.pgvector

import org.vitrivr.engine.core.database.retrievable.AbstractRetrievableInitializerTest

/**
 * An [AbstractRetrievableInitializerTest] for the [PgVectorConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class RetrievableInitializerTest : AbstractRetrievableInitializerTest("test-schema-postgres.json") {
    /**
     * Checks if tables initialized actually exists.
     *
     * @return True if tables exist, false otherwise.
     */
    override fun checkTablesExist(): Boolean {
        /* Check for existence of tables. */
        (this.testConnection as PgVectorConnection).jdbc.prepareStatement("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)").use { statement ->
            /* Check existence of retrievable table. */
            statement.setString(1, SCHEMA_NAME)
            statement.setString(2, RETRIEVABLE_ENTITY_NAME)
            statement.executeQuery().use { result ->
                if (!result.next() || !result.getBoolean(1)) {
                    return false
                }
            }

            /* Check existence of relationship table. */
            statement.setString(1, SCHEMA_NAME)
            statement.setString(2, RELATIONSHIP_ENTITY_NAME)
            statement.executeQuery().use { result ->
                if (!result.next() || !result.getBoolean(1)) {
                    return false
                }
            }
        }
        return true
    }
}