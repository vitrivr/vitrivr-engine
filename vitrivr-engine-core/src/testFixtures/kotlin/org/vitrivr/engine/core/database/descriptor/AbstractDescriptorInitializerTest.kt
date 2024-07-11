package org.vitrivr.engine.core.database.descriptor

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.database.AbstractDatabaseTest
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorInitializerTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {
    /** The [Schema.Field] used for this [AbstractVectorDescriptorInitializerTest]. */
    protected abstract val field: Schema.Field<*, *>

    /**
     * Tests if the [DescriptorInitializer.isInitialized] works as expected.,
     */
    @Test
    fun testIsInitializedWithoutInitialization() {
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(this.field).isInitialized())
    }

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    @Test
    fun testInitializeEntities() {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(this.field).isInitialized())

        /* Initialize basic tables. */
        this.testConnection.getDescriptorInitializer(this.field).initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.testConnection.getDescriptorInitializer(this.field).isInitialized())
    }

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    @Test
    fun testDeInitializeEntities() {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(this.field).isInitialized())

        /* Initialize basic tables. */
        this.testConnection.getDescriptorInitializer(this.field).initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.testConnection.getDescriptorInitializer(this.field).isInitialized())

        this.testConnection.getDescriptorInitializer(this.field).deinitialize()
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(this.field).isInitialized())
    }

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.testSchema.connection.getDescriptorInitializer(this.field).deinitialize()
    }
}