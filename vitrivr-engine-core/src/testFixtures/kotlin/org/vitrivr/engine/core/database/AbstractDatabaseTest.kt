package org.vitrivr.engine.core.database

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.vitrivr.engine.core.config.schema.SchemaConfig
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.SchemaManager

/**
 * Abstract base class for database tests.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractDatabaseTest(schemaPath: String) {
    companion object {
        protected const val SCHEMA_NAME = "vitrivr-test"
    }

    /** [SchemaManager] for this [AbstractDatabaseTest]. */
    protected val manager = SchemaManager()

    init {
        /* Loads schema. */
        val schema = SchemaConfig.loadFromResource(schemaPath)
        this.manager.load(SCHEMA_NAME, schema)
    }

    /** The test [Schema]. */
    protected val testSchema: Schema = this.manager.getSchema("vitrivr-test") ?: throw IllegalArgumentException("Schema 'vitrivr-test' not found!")

    /** The [Connection] object backing the [Schema]. */
    protected val testConnection = this.testSchema.connection

    /**
     * Closes the connection after each test.
     */
    @AfterAll
    fun disconnect() {
        this.testConnection.close()
    }
}