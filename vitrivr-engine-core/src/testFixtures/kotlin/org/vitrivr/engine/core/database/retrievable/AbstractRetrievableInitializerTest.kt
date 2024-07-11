package org.vitrivr.engine.core.database.retrievable

import org.junit.jupiter.api.AfterEach
import org.vitrivr.engine.core.database.AbstractDatabaseTest

/**
 * An abstract set of test cases to test the proper functioning of [RetrievableInitializer] implementations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractRetrievableInitializerTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {
    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    abstract fun testIsInitializedWithoutInitialization()

    /**
     * Tests if the [RetrievableInitializer] can be initialized without throwing an exception. Furthermore,
     * the test should check if the necessary tables have been created.
     */
    abstract fun testInitializeEntities()

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.schema.connection.getRetrievableInitializer().deinitialize()
        for (field in this.schema.fields()) {
            field.getInitializer().deinitialize()
        }
    }
}