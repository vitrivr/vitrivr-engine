package org.vitrivr.engine.core.database.retrievable

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.database.AbstractDatabaseTest
import org.vitrivr.engine.core.model.retrievable.Ingested
import java.util.*

/**
 * An abstract set of test cases to test the proper functioning of [RetrievableWriter] implementations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractRetrievableWriterTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {
    /**
     * Tests if the [RetrievableWriter.add] works as expected.
     */
    @Test
    fun testAdd() {
        val writer = this.testConnection.getRetrievableWriter()
        val reader = this.testConnection.getRetrievableReader()

        /* Create and add retrievable. */
        val id = UUID.randomUUID()
        Assertions.assertTrue(writer.add(Ingested(id, "INGESTED:TEST", false)))

        /* Check if retrievable can be read. */
        Assertions.assertEquals(1L, reader.count())
        Assertions.assertEquals("INGESTED:TEST", reader[id]?.type)
    }

    /**
     * Tests if the [RetrievableWriter.add] works as expected.
     */
    @Test
    fun testAddAll() {
        val writer = this.testConnection.getRetrievableWriter()
        val reader = this.testConnection.getRetrievableReader()
        val size = Random().nextInt(500, 5000)

        /* Create and add retrievable. */
        val ids = (0 until size).map { UUID.randomUUID() }
        val ingested = ids.map { Ingested(it, "INGESTED:TEST", false) }
        Assertions.assertTrue(writer.addAll(ingested))

        /* Check if retrievable can be read. */
        Assertions.assertEquals(ids.size.toLong(), reader.count())
        reader.getAll(ids).forEachIndexed { i, it ->
            Assertions.assertEquals(ids[i], it.id)
            Assertions.assertEquals("INGESTED:TEST", it.type)
        }
    }

    /**
     * Tests if the [RetrievableWriter.add] works as expected.
     */
    @Test
    fun testDelete() {
        val writer = this.testConnection.getRetrievableWriter()
        val reader = this.testConnection.getRetrievableReader()
        val size = Random().nextInt(500, 5000)

        /* Create and add retrievable. */
        val ids = (0 until size).map { UUID.randomUUID() }
        val ingested = ids.map { Ingested(it, "INGESTED:TEST", false) }
        val delete = ingested[Random().nextInt(0, ingested.size)]

        /* Execute actions. */
        Assertions.assertFalse(writer.delete(delete))
        Assertions.assertTrue(writer.addAll(ingested))
        Assertions.assertTrue(writer.delete(delete))

        /* Check if retrievable can be read. */
        Assertions.assertEquals(ids.size.toLong() - 1L, reader.count())
        reader.getAll(ids).forEachIndexed() { i, it ->
            Assertions.assertNotEquals(delete.id, it.id)
            Assertions.assertEquals("INGESTED:TEST", it.type)
        }
    }

    /**
     * Tests if the [RetrievableWriter.add] works as expected.
     */
    @Test
    fun testDeleteAll() {
        val writer = this.testConnection.getRetrievableWriter()
        val reader = this.testConnection.getRetrievableReader()
        val size = Random().nextInt(500, 5000)

        /* Create and add retrievable. */
        val ids = (0 until size).map { UUID.randomUUID() }
        val ingested = ids.map { Ingested(it, "INGESTED:TEST", false) }

        /* Execute actions. */
        Assertions.assertTrue(writer.addAll(ingested))

        /* Check if retrievable can be read. */
        Assertions.assertEquals(ids.size.toLong(), reader.count())
        reader.getAll(ids).forEachIndexed() { i, it ->
            Assertions.assertEquals("INGESTED:TEST", it.type)
        }

        /* Execute actions. */
        Assertions.assertTrue(writer.deleteAll(ingested))
        Assertions.assertEquals(0L, reader.count())
    }


    /**
     * Tests if the [RetrievableWriter.add] works as expected.
     */
    @Test
    fun testUpdate() {
        val writer = this.testConnection.getRetrievableWriter()
        val reader = this.testConnection.getRetrievableReader()
        val size = Random().nextInt(500, 5000)

        /* Create and add retrievable. */
        val ids = (0 until size).map { UUID.randomUUID() }
        val ingested = ids.map { Ingested(it, "INGESTED:TEST", false) }
        val update = ingested[Random().nextInt(0, ingested.size)]

        /* Execute actions. */
        Assertions.assertTrue(writer.addAll(ingested))

        /* Check if retrievable can be read. */
        Assertions.assertEquals(ids.size.toLong(), reader.count())
        Assertions.assertTrue(writer.update(update.copy(type = "INGESTED:TEST2")))

        /* Execute actions. */
        Assertions.assertEquals(ids.size.toLong(), reader.count())
        Assertions.assertEquals("INGESTED:TEST2", reader[update.id]?.type)
    }

    /**
     * Cleans up the database after each test.
     */
    @BeforeEach
    open fun prepare() {
        this.testSchema.connection.getRetrievableInitializer().initialize()
    }

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.testSchema.connection.getRetrievableInitializer().deinitialize()
    }
}