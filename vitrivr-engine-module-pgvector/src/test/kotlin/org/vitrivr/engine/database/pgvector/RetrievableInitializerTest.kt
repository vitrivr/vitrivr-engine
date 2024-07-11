package org.vitrivr.engine.database.pgvector

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.database.retrievable.AbstractRetrievableInitializerTest

/**
 * An [AbstractRetrievableInitializerTest] for the [PgVectorConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class RetrievableInitializerTest : AbstractRetrievableInitializerTest("test-schema-postgres.json") {

    /** Internal [PgVectorConnection] object. */
    private val connection = this.schema.connection as? PgVectorConnection ?: throw IllegalArgumentException("Schema 'vitrivr-test' not found!")

    @Test
    override fun testIsInitializedWithoutInitialization() {
        Assertions.assertFalse(this.connection.getRetrievableInitializer().isInitialized())
    }

    @Test
    override fun testInitializeEntities() {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.connection.getRetrievableInitializer().isInitialized())

        /* Initialize basic tables. */
        this.connection.getRetrievableInitializer().initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.connection.getRetrievableInitializer().isInitialized())

        /* Check for existence of tables. */
        this.connection.jdbc.prepareStatement("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)").use { statement ->
            /* Check existence of retrievable table. */
            statement.setString(1, SCHEMA_NAME)
            statement.setString(2, RETRIEVABLE_ENTITY_NAME)
            statement.executeQuery().use { result ->
                Assertions.assertTrue(result.next() && result.getBoolean(1))
            }

            /* Check existence of relationship table. */
            statement.setString(1, SCHEMA_NAME)
            statement.setString(2, RELATIONSHIP_ENTITY_NAME)
            statement.executeQuery().use { result ->
                Assertions.assertTrue(result.next() && result.getBoolean(1))
            }
        }
    }
}