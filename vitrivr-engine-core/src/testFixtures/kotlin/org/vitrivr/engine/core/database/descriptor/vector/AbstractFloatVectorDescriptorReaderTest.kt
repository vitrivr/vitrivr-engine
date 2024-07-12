package org.vitrivr.engine.core.database.descriptor.vector

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.vitrivr.engine.core.database.AbstractDatabaseTest
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.SortOrder
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A series of test cases the test the functionality of the [VectorDescriptorReader].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractFloatVectorDescriptorReaderTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {

    /** The [Schema.Field] used for this [DescriptorInitializerTest]. */
    private val field: Schema.Field<*, FloatVectorDescriptor> = this.testSchema["averagecolor"]!! as Schema.Field<*, FloatVectorDescriptor>

    /**
     * Tests nearest neighbour search through the [VectorDescriptorReader.query] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    fun testNearestNeighbourSearch(distance: Distance) {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()
        val size = random.nextInt(500, 5000)

        /* Generate and store test data. */
        val retrievables = (0 until size).map {
            Ingested(UUID.randomUUID(), "SOURCE:TEST", true)
        }
        Assertions.assertTrue(this.testConnection.getRetrievableWriter().addAll(retrievables))

        val descriptors = retrievables.map {
            FloatVectorDescriptor(UUID.randomUUID(), it.id, Value.FloatVector(FloatArray(3) { random.nextFloat() }))
        }
        Assertions.assertTrue(writer.addAll(descriptors))

        /* Perform nearest neighbour search. */
        val query = ProximityQuery(
            Value.FloatVector(FloatArray(3) { random.nextFloat() }),
            distance,
            SortOrder.ASC,
            100,
            fetchVector = true
        )
        val result = reader.query(query).toList()

        /* Make manual query and compare. */
        val manual = descriptors.sortedBy { distance(it.vector, query.value) }.take(100)
        result.zip(manual).forEach {
            Assertions.assertEquals(it.first.id, it.second.id)
        }
    }

    /**
     * Tests farthest neighbour search through the [VectorDescriptorReader.query] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    fun testFarthestNeighbourSearch(distance: Distance) {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()
        val size = random.nextInt(500, 5000)

        /* Generate and store test data. */
        val retrievables = (0 until size).map {
            Ingested(UUID.randomUUID(), "SOURCE:TEST", true)
        }
        Assertions.assertTrue(this.testConnection.getRetrievableWriter().addAll(retrievables))

        val descriptors = retrievables.map {
            FloatVectorDescriptor(UUID.randomUUID(), it.id, Value.FloatVector(FloatArray(3) { random.nextFloat() }))
        }
        Assertions.assertTrue(writer.addAll(descriptors))

        /* Perform nearest neighbour search. */
        val query = ProximityQuery(
            Value.FloatVector(FloatArray(3) { random.nextFloat() }),
            distance,
            SortOrder.DESC,
            100,
            fetchVector = true
        )
        val result = reader.query(query).toList()

        /* Make manual query and compare. */
        val manual = descriptors.sortedByDescending { distance(it.vector, query.value) }.take(100)
        result.zip(manual).forEach {
            Assertions.assertEquals(it.first.id, it.second.id)
        }
    }

    /**
     * Cleans up the database after each test.
     */
    @BeforeEach
    open fun prepare() {
        this.testConnection.getRetrievableInitializer().initialize()
        this.testConnection.getDescriptorInitializer(this.field).initialize()
    }

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.testConnection.getRetrievableInitializer().deinitialize()
        this.testConnection.getDescriptorInitializer(this.field).deinitialize()
    }
}