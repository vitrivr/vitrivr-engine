package org.vitrivr.engine.core.database.retrievable

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.database.AbstractDatabaseTest

/**
 * An abstract set of test cases to test the proper functioning of [RetrievableInitializer] implementations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractRetrievableInitializerTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {
    /**
     * Tests if the [RetrievableInitializer.isInitialized] works as expected.,
     */
    @Test
    fun testIsInitializedWithoutInitialization() {
        Assertions.assertFalse(this.testConnection.getRetrievableInitializer().isInitialized())
    }

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    @Test
    fun testInitializeEntities() {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.testConnection.getRetrievableInitializer().isInitialized())

        /* Initialize basic tables. */
        this.testConnection.getRetrievableInitializer().initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.testConnection.getRetrievableInitializer().isInitialized())
        Assertions.assertTrue(this.checkTablesExist())
    }

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    @Test
    fun testDeInitializeEntities() {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.testConnection.getRetrievableInitializer().isInitialized())

        /* Initialize basic tables. */
        this.testConnection.getRetrievableInitializer().initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.testConnection.getRetrievableInitializer().isInitialized())
        Assertions.assertTrue(this.checkTablesExist())

        this.testConnection.getRetrievableInitializer().deinitialize()
        Assertions.assertFalse(this.testConnection.getRetrievableInitializer().isInitialized())
        Assertions.assertFalse(this.checkTablesExist())
    }

    /**
     * Checks if tables initialized actually exists.
     *
     * This check is database specific and therefore abstract.
     *
     * @return True if tables exist, false otherwise.
     */
    protected abstract fun checkTablesExist(): Boolean

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.testSchema.connection.getRetrievableInitializer().deinitialize()
    }
}