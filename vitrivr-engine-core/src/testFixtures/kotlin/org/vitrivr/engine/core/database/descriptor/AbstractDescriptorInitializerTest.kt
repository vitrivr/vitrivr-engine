package org.vitrivr.engine.core.database.descriptor

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.vitrivr.engine.core.database.AbstractDatabaseTest
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.metamodel.Schema
import java.util.stream.Stream

/**
 * An abstract test to test the functionality of the [DescriptorInitializer].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractDescriptorInitializerTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {
    /**
     * Tests if the [DescriptorInitializer.isInitialized] works as expected.,
     */
    @ParameterizedTest
    @MethodSource("getFields")
    fun testIsInitializedWithoutInitialization(field: Schema.Field<*, *>) {
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(field).isInitialized())
    }

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    @ParameterizedTest
    @MethodSource("getFields")
    fun testInitializeEntities(field: Schema.Field<*, *>) {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(field).isInitialized())

        /* Initialize basic tables. */
        this.testConnection.getDescriptorInitializer(field).initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.testConnection.getDescriptorInitializer(field).isInitialized())
    }

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    @ParameterizedTest
    @MethodSource("getFields")
    fun testDeInitializeEntities(field: Schema.Field<*, *>) {
        /* Check initialization status (should be false). */
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(field).isInitialized())

        /* Initialize basic tables. */
        this.testConnection.getDescriptorInitializer(field).initialize()

        /* Check initialization status (should be true). */
        Assertions.assertTrue(this.testConnection.getDescriptorInitializer(field).isInitialized())

        this.testConnection.getDescriptorInitializer(field).deinitialize()
        Assertions.assertFalse(this.testConnection.getDescriptorInitializer(field).isInitialized())
    }

    /**
     * Prepares the database before each test.
     */
    @BeforeEach
    open fun prepare() {
        this.testSchema.connection.getRetrievableInitializer().initialize()
    }

    /**
     * Cleans up the database after each test.
     */
    @AfterEach
    open fun cleanup() {
        for (field in this.testSchema.fields()) {
            this.testSchema.connection.getDescriptorInitializer(field).deinitialize()
        }
        this.testSchema.connection.getRetrievableInitializer().deinitialize()
    }

    /**
     * Returns a [Stream] of [Schema.Field]s that are part of the test schema.
     *
     * @return [Stream] of [Schema.Field]s
     */
    private fun getFields(): Stream<Schema.Field<*, *>> = this.testSchema.fields().map { it as Schema.Field<*, *> }.stream()
}